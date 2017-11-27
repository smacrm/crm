/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import gnext.rest.iteply.bean.TelCustomer;
import gnext.utils.Console;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gnextadmin
 */
public class HTTPClient {
    final private Logger logger = LoggerFactory.getLogger(HTTPClient.class);
    private String SHOPDB_HOST;//"http://192.168.1.170/zend0/index.php/shopdb?param=value"
    private String APP_HOST;
    private String CALL_REST = "/rest/itelpy/";
    private String CALL_METHOD = "?act=StartItelpy";
    private String[] params;//productName1,shopdb_address_tab_5
    private String[] paramVals;

    public HTTPClient(String inHost) {
        if(StringUtils.isBlank(inHost)) return;
        this.APP_HOST = inHost;
    }

    public HTTPClient(String inHost, String[] inParams, String[] inParamVals) {
        if(StringUtils.isBlank(inHost)) return;
        this.SHOPDB_HOST = inHost;
        if(inParams != null && inParamVals != null
                && inParams.length == inParamVals.length) {
            this.params = inParams;
            this.paramVals = inParamVals;
        }
    }

    public boolean call(TelCustomer customer) {
        Console.log("===== HTTP CALL POST Start =====");
        if(StringUtils.isBlank(this.APP_HOST)) return false;
        HttpURLConnection con = null;
        try {
            URL url = new URL(this.APP_HOST + CALL_REST);
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");

            BufferedWriter writer =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    con.getOutputStream(),
                                    StandardCharsets.UTF_8));

            StringBuilder bui = new StringBuilder();
            bui.append(CALL_METHOD);
            Field[] custs = customer.getClass().getDeclaredFields();
            for(Field field:custs) {
                String col = field.getName();
                if(StringUtils.isBlank(col)) continue;
                try {
                    field.setAccessible(true);
                    Object obj = field.get(customer);
                    String val = String.valueOf(obj);
                    if(StringUtils.isBlank(val)) continue;
                    bui.append("&").append(col).append("=").append(val);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    logger.error("HTTPClient.call().customer", ex);
                }
            }
            writer.write(bui.toString());
            writer.flush();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK
                    || con.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                Console.log("===== HTTP CALL POST True =====");
            } else {
                Console.log("===== HTTP CALL POST False =====");
                return false;
            }
        } catch (IOException ex) {
            logger.error("HTTPClient.call()", ex);
            return false;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return true;
    }

    public boolean get() {
        Console.log("===== HTTP GET Start =====");
        try {
            StringBuilder bui = new StringBuilder();
            bui.append(this.SHOPDB_HOST);
            bui.append(getParams(this.params, this.paramVals));
//            if(this.params != null && this.params.length > 0) {
//                for(int i=0; i<this.params.length ; i++) {
//                    if(StringUtils.isBlank(this.params[i])) continue;
//                    if(i == 0) {
//                        bui.append("?").append(this.params[i]).append("=").append(this.paramVals[i]);
//                    } else {
//                        bui.append("&").append(this.params[i]).append("=").append(this.paramVals[i]);
//                    }
//                }
//            }

            URL url = new URL(bui.toString());
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return true;
//                    try (InputStreamReader isr =
//                            new InputStreamReader(
//                                    con.getInputStream(),
//                                    StandardCharsets.UTF_8);
//                        BufferedReader reader = new BufferedReader(isr)) {
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            Console.log(line);
//                        }
//                    }
                }
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        } catch (IOException ex) {
            logger.error("HTTPClient.get()", ex);
            return false;
        }
        Console.log("===== HTTP GET End =====");
        return true;
    }

    public JsonObject post() {
        Console.log("===== HTTP POST Start =====");
        try {
            if(StringUtils.isBlank(this.SHOPDB_HOST)) return null;
            URL url = new URL(this.SHOPDB_HOST);
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");

                BufferedWriter writer =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        con.getOutputStream(),
                                        StandardCharsets.UTF_8));

                StringBuilder bui = getParams(this.params, this.paramVals);
                writer.write(bui.toString());
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK
                        || con.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                    StringBuilder sb = new StringBuilder();
                    try (InputStreamReader isr =
                            new InputStreamReader(
                                    con.getInputStream(),
                                    StandardCharsets.UTF_8);
                        BufferedReader reader = new BufferedReader(isr)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            //sb.append(line);
                            Console.log(sb.append(line));
                        }
                    }
                    sb.append("{\"result\":[{ \"label\" : \"label1\", \"value\"  : \"value1\" }, { \"label\" : \"label2\", \"value\"  : \"value2\" }]}");
                    Console.log("===== HTTP POST End =====");
                    JsonObject json = new JsonParser().parse(sb.toString()).getAsJsonObject();
                    return json;
                } else {
                    Console.log("===== HTTP POST False =====");
                    return null;
                }
            } catch (JsonSyntaxException ex) {
                logger.info("HTTPClient.post().getAsJsonObject()", ex);
                return null;
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        } catch (IOException ex) {
            logger.info("HTTPClient.post()", ex);
            return null;
        }
    }

    private StringBuilder getParams(String[] inParams, String[] inParamVals) {
        StringBuilder bui = new StringBuilder();
        if(inParams == null && inParamVals == null
                || inParams.length != inParamVals.length) return bui;
        for(int i=0; i<inParams.length ; i++) {
            if(StringUtils.isBlank(inParams[i])) continue;
            if(i == 0) {
                bui.append("?").append(inParams[i]).append("=").append(inParamVals[i]);
            } else {
                bui.append("&").append(inParams[i]).append("=").append(inParamVals[i]);
            }
        }
        return bui;
    }
}
