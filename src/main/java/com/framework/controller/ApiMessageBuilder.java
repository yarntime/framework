package com.framework.controller;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import com.framework.exception.ApiServerException;
import com.framework.exception.BaseException;
import com.framework.exception.ErrorCode;
import com.framework.exception.HttpRequestException;
import com.framework.exception.InvalidParameterValueException;
import com.framework.message.Message;
import com.framework.utils.DateUtil;
import com.framework.utils.ReflectUtil;

public class ApiMessageBuilder {

    private static Logger logger = Logger.getLogger(ApiMessageBuilder.class);

    public static Message processParameters(String action, Class<? extends Message> clazz,
            Map<String, String> params) throws Exception {
        Map<String, Object> unpackedParams = unpackParams(params);
        Message msg = clazz.newInstance();
        List<Field> fields = ReflectUtil.getAllFieldsForClass(clazz, Message.class);

        for (Field field : fields) {
            Parameter parameterAnnotation = field.getAnnotation(Parameter.class);
            if ((parameterAnnotation == null) || !parameterAnnotation.expose()) {
                continue;
            }

            // TODO: Annotate @Validate on API msg classes, FIXME how to process
            // Validate
            Object paramObj = unpackedParams.get(parameterAnnotation.name().toLowerCase());
            if (paramObj == null) {
                if (parameterAnnotation.required()) {
                    throw new HttpRequestException("Unable to execute API command "
                            + msg.getMessageType() + " due to missing parameter "
                            + parameterAnnotation.name(), HttpStatus.SC_BAD_REQUEST);
                }
                continue;
            }

            // marshall the parameter into the correct type and set the field
            // value
            try {
                setFieldValue(field, msg, paramObj, parameterAnnotation);
            } catch (IllegalArgumentException argEx) {
                argEx.printStackTrace();
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to execute API command " + msg.getMessageType()
                            + " due to invalid value " + paramObj + " for parameter "
                            + parameterAnnotation.name());
                }
                throw new HttpRequestException("Unable to execute API command "
                        + msg.getMessageType() + " due to invalid value " + paramObj
                        + " for parameter " + parameterAnnotation.name(), HttpStatus.SC_BAD_REQUEST);
            } catch (ParseException parseEx) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid date parameter " + paramObj + " passed to command "
                            + msg.getMessageType());
                }
                throw new HttpRequestException("Unable to parse date " + paramObj + " for command "
                        + msg.getMessageType()
                        + ", please pass dates in the format mentioned in the api documentation",
                        HttpStatus.SC_BAD_REQUEST);
            } catch (InvalidParameterValueException invEx) {
                throw new InvalidParameterValueException("Unable to execute API command "
                        + msg.getMessageType() + " due to invalid value. " + invEx.getMessage());
            } catch (RuntimeException exception) {
                logger.error("RuntimeException", exception);
                // FIXME: Better error message? This only happens if the API
                // command is not executable, which typically
                // means
                // there was
                // and IllegalAccessException setting one of the parameters.
                throw new BaseException(ErrorCode.INTERNAL_ERROR,
                        "Internal error handle params to message " + msg.getMessageType());
            }
        }
        return msg;
    }

    // FIXME: move this to a utils method so that maps can be unpacked and
    // integer/long values can be appropriately cast
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Object> unpackParams(Map<String, String> params) {
        Map<String, Object> lowercaseParams = new HashMap<String, Object>();
        for (String key : params.keySet()) {
            int arrayStartIndex = key.indexOf('[');
            int arrayStartLastIndex = key.lastIndexOf('[');
            if (arrayStartIndex != arrayStartLastIndex) {
                throw new ApiServerException(
                        "Unable to decode parameter "
                                + key
                                + "; if specifying an object array, please use parameter[index].field=XXX, e.g. userGroupList[0].group=httpGroup");
            }

            if (arrayStartIndex > 0) {
                int arrayEndIndex = key.indexOf(']');
                int arrayEndLastIndex = key.lastIndexOf(']');
                if ((arrayEndIndex < arrayStartIndex) || (arrayEndIndex != arrayEndLastIndex)) {
                    // malformed parameter
                    throw new ApiServerException(
                            "Unable to decode parameter "
                                    + key
                                    + "; if specifying an object array, please use parameter[index].field=XXX, e.g. userGroupList[0].group=httpGroup");
                }

                // Now that we have an array object, check for a field name in
                // the case of a complex object
                int fieldIndex = key.indexOf('.');
                String fieldName = null;
                if (fieldIndex < arrayEndIndex) {
                    throw new ApiServerException(
                            "Unable to decode parameter "
                                    + key
                                    + "; if specifying an object array, please use parameter[index].field=XXX, e.g. userGroupList[0].group=httpGroup");
                } else {
                    fieldName = key.substring(fieldIndex + 1);
                }

                // parse the parameter name as the text before the first '['
                // character
                String paramName = key.substring(0, arrayStartIndex);
                paramName = paramName.toLowerCase();

                Map<Integer, Map> mapArray = null;
                Map<String, Object> mapValue = null;
                String indexStr = key.substring(arrayStartIndex + 1, arrayEndIndex);
                int index = 0;
                boolean parsedIndex = false;
                try {
                    if (indexStr != null) {
                        index = Integer.parseInt(indexStr);
                        parsedIndex = true;
                    }
                } catch (NumberFormatException nfe) {
                    logger.warn("Invalid parameter " + key
                            + " received, unable to parse object array, returning an error.");
                }

                if (!parsedIndex) {
                    throw new ApiServerException(
                            "Unable to decode parameter "
                                    + key
                                    + "; if specifying an object array, please use parameter[index].field=XXX, e.g. userGroupList[0].group=httpGroup");
                }

                Object value = lowercaseParams.get(paramName);
                if (value == null) {
                    // for now, assume object array with sub fields
                    mapArray = new HashMap<Integer, Map>();
                    mapValue = new HashMap<String, Object>();
                    mapArray.put(Integer.valueOf(index), mapValue);
                } else if (value instanceof Map) {
                    mapArray = (HashMap) value;
                    mapValue = mapArray.get(Integer.valueOf(index));
                    if (mapValue == null) {
                        mapValue = new HashMap<String, Object>();
                        mapArray.put(Integer.valueOf(index), mapValue);
                    }
                }

                // we are ready to store the value for a particular field into
                // the map for this object
                mapValue.put(fieldName, params.get(key));

                lowercaseParams.put(paramName, mapArray);
            } else {
                lowercaseParams.put(key.toLowerCase(), params.get(key));
            }
        }
        return lowercaseParams;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setFieldValue(Field field, Message msg, Object paramObj,
            Parameter annotation) throws IllegalArgumentException, ParseException,
            InvalidParameterValueException, HttpRequestException {
        try {
            field.setAccessible(true);
            CommandType fieldType = annotation.type();
            switch (fieldType) {
                case BOOLEAN:
                    field.set(msg, Boolean.valueOf(paramObj.toString()));
                    break;
                case DATE:
                    // This piece of code is for maintaining backward compatibility
                    // and support both the date formats(Bug 9724)
                    // Do the date messaging for ListEventsCmd only
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setLenient(false);
                    synchronized (format) {
                        field.set(msg, format.parse(paramObj.toString()));
                    }
                    break;
                case FLOAT:
                    // Assuming that the parameters have been checked for required
                    // before now,
                    // we ignore blank or null values and defer to the command to
                    // set a default
                    // value for optional parameters ...
                    if (paramObj != null && isNotBlank(paramObj.toString())) {
                        field.set(msg, Float.valueOf(paramObj.toString()));
                    }
                    break;
                case INTEGER:
                    // Assuming that the parameters have been checked for required
                    // before now,
                    // we ignore blank or null values and defer to the command to
                    // set a default
                    // value for optional parameters ...
                    if (paramObj != null && isNotBlank(paramObj.toString())) {
                        field.set(msg, Integer.valueOf(paramObj.toString()));
                    }
                    break;
                case LIST:
                    List listParam = new ArrayList();
                    StringTokenizer st = new StringTokenizer(paramObj.toString(), ",");
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        CommandType listType = annotation.collectionType();
                        switch (listType) {
                            case INTEGER:
                                listParam.add(Integer.valueOf(token));
                                break;
                            case UUID:
                                // TODO:not use
                                // if (token.isEmpty())
                                // break;
                                // Long internalId = translateUuidToInternalId(token,
                                // annotation);
                                // listParam.add(internalId);
                                break;
                            case LONG: {
                                listParam.add(Long.valueOf(token));
                            }
                                break;
                            case SHORT:
                                listParam.add(Short.valueOf(token));
                            case STRING:
                                listParam.add(token);
                                break;
                            default:
                                break;
                        }
                    }
                    field.set(msg, listParam);
                    break;
                case UUID:
                    // if (paramObj.toString().isEmpty())
                    // break;
                    // Long internalId = translateUuidToInternalId(
                    // paramObj.toString(), annotation);
                    // field.set(msg, internalId);
                    // NOW do not use UUID
                    break;
                case LONG:
                    field.set(msg, Long.valueOf(paramObj.toString()));
                    break;
                case SHORT:
                    field.set(msg, Short.valueOf(paramObj.toString()));
                    break;
                case STRING:
                    if ((paramObj != null) && paramObj.toString().length() > annotation.length()) {
                        logger.error("Value greater than max allowed length " + annotation.length()
                                + " for param: " + field.getName());
                        throw new InvalidParameterValueException(
                                "Value greater than max allowed length " + annotation.length()
                                        + " for param: " + field.getName());
                    }
                    if (annotation.required()) {
                        if (paramObj == null || "".equals(paramObj.toString().trim()))
                            throw new HttpRequestException("paramter " + annotation.name()
                                    + " is required,null or empty string like \"\" is not allowed",
                                    HttpStatus.SC_BAD_REQUEST);
                    }

                    field.set(msg, paramObj.toString());
                    break;
                case TZDATE:
                    field.set(msg, DateUtil.parseTZDateString(paramObj.toString()));
                    break;
                case MAP:
                default:
                    field.set(msg, paramObj);
                    break;
            }
        } catch (IllegalAccessException ex) {
            logger.error("Error initializing command " + msg.getMessageType() + ", field "
                    + field.getName() + " is not accessible.");
            throw new RuntimeException("Internal error initializing parameters for command "
                    + msg.getMessageType() + " [field " + field.getName() + " is not accessible]");
        }
    }

    private static boolean isNotBlank(String s) {
        return s != null && s.trim().length() != 0;
    }

}