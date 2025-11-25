package org.example.gstmeetapi.helper.response.entity;

public class HttpStatusHelper {
    public static final ResponseType SUCCESS = new ResponseType(1200, "SUCCESS");
    public static final ResponseType SUCCESS_CREATE = new ResponseType(201, "SUCCESS");
    public static final ResponseType BAD_REQUEST = new ResponseType(400, "BAD REQUEST");
    public static final ResponseType UNAUTHORIZED = new ResponseType(401, "UNAUTHORIZED");
    public static final ResponseType NOTFOUND = new ResponseType(1404, "NOT FOUND");
    public static final ResponseType TIME_OUT = new ResponseType(408, "REQUEST TIME OUT");
    public static final ResponseType FORBIDDEN = new ResponseType(403, "FORBIDDEN");
    public static final ResponseType HAS_AREA = new ResponseType(1206, "HAS EXITS");
    public static final ResponseType TIME_EXPIRES = new ResponseType(406, "TIME EXPIRES"); //Expires
    public static final ResponseType CONFLICT = new ResponseType(409, "CONFLICT"); //Expires
    public static final ResponseType INTERNAL_SERVER_ERROR = new ResponseType(1500, "INTERNAL_SERVER_ERROR"); //Expires
}