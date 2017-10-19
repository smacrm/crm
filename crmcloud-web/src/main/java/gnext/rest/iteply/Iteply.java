package gnext.rest.iteply;

import com.google.gson.Gson;
import gnext.bean.Member;
import gnext.service.MemberService;
import gnext.rest.iteply.bean.History;
import gnext.rest.iteply.bean.Itelpy;
import gnext.rest.iteply.bean.KeyValue;
import gnext.rest.iteply.bean.ServiceNumber;
import gnext.rest.iteply.bean.Setting;
import gnext.rest.iteply.bean.TelCustomer;
import gnext.rest.iteply.bean.UserInfo;
import gnext.rest.iteply.bean.Voice;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 * @since Nov 1, 2016
 */
@Path("/itelpy")
@Produces(MediaType.TEXT_PLAIN)
public class Iteply implements ItelpyService{

    @EJB private MemberService memberServiceImpl;
    
    @GET
    public void index(@Context HttpServletRequest request, 
                        @Context HttpServletResponse response,
                        @Context UriInfo info, 
                        @QueryParam("act") String act) throws URISyntaxException, ServletException, IOException {
        UriBuilder location = info.getAbsolutePathBuilder().path(act);
        info.getQueryParameters().forEach((key, value) -> {
            if( !"act".equals(key) && !value.isEmpty() ){
                location.queryParam(key, value.get(0));
            }
        });
        String path = location.build().toString();
        //System.err.println(">>" + path);
        request.getRequestDispatcher(path.substring(path.indexOf("/rest"))).forward(request, response);
    }
    
    @GET
    @Path("CheckUserExists")
    public Response checkUserExists(
            @DefaultValue("") @QueryParam("userID") String userID
    ) {
        return Response.ok().entity(memberServiceImpl.findByUsername(userID) != null ? 1 : 0).build();
    }
    
    @GET
    @Path("GetUserInfo")
    public Response getUserInfo(
            @QueryParam("userID") String userID		//ユーザID
        ){
        Member m = memberServiceImpl.findByUsername(userID);
        if(m != null){
            UserInfo info = new UserInfo();
            info.setFirstName(m.getMemberNameLast());
            info.setFamilyName(m.getMemberNameFirst());
            return Response.ok().entity(new Gson().toJson(info)).build();
        }
        return Response.noContent().build();
    } 
    
    @GET
    @Path("GetSetting")
    public Response getSetting(){
        
        Setting bean = new Setting(2432);
        
        ServiceNumber serviceNumberList;
        
        serviceNumberList = new ServiceNumber("gr1");
        serviceNumberList.getTelNumbers().add(new KeyValue("0312340001", "03-1234-0001"));
        bean.getServiceNumberList().add(serviceNumberList);
        
        serviceNumberList = new ServiceNumber("gr2");
        serviceNumberList.getTelNumbers().add(new KeyValue("0312340002", "03-1234-0002"));
        bean.getServiceNumberList().add(serviceNumberList);
        
        bean.getTelePhoneDirectory().add(new KeyValue("01", "0312340001"));
        bean.getTelePhoneDirectory().add(new KeyValue("02", "0312340002"));
        
        return Response.ok().entity(new Gson().toJson(bean)).build();
    }
    
    @GET
    @Path("GetCustomer")
    public Response getCustomer(
            @QueryParam("otherTelNumber") String otherTelNumber     //顧客の電話番号
        ){
        TelCustomer bean = new TelCustomer();
        bean.setCode("0001");
        bean.setLastNameKana("一場");
        bean.setLastNameKanji("一場");
        bean.setFirstNameKana("一場");
        bean.setFirstNameKanji("一場");
        bean.setSpecialtyLastNameKana("一場");
        bean.setSpecialtyLastNameKanji("一場");
        bean.setSpecialtyFirstNameKana("一場");
        bean.setSpecialtyFirstNameKanji("一場");
        bean.setTelNumber(otherTelNumber);
        bean.setIssueReceiveLargeName("0001");
        bean.setNote("Note");
        
        return Response.ok().entity(new Gson().toJson(bean)).build();
    } 
    
    @GET
    @Path("GetHistoryList")
    public Response getHistoryList(
            @QueryParam("recordNum") String recordNum,  //一度に取得する最大レコード数
            @QueryParam("inOutFlag") String inOutFlag,  //1:着信　0:発信　指定無し:両方
            @QueryParam("userID") String userID,        //ユーザID	省略した場合はすべてのオペレータ
            @QueryParam("date") String date             //取得するリストの年月日YYYY-MM-DD形式　省略した場合はすべての日が対象
        ){
        
        Map<String, String> entity = new HashMap<>();
        entity.put("RecovoiceID", "1394");
        
        List<History> bean = new ArrayList<>();
        History his = new History();
        bean.add(his);
        
        return Response.ok().entity(new Gson().toJson(bean)).build();
    }
    
    @GET
    @Path("AddHistory")
    public Response addHistory(
            @QueryParam("uniqueID") String uniqueID,
            @QueryParam("userID") String userID,                    //ユーザID
            @QueryParam("otherTelNumber") String otherTelNumber,    //相手側の電話番号
            @QueryParam("myTelNumber") String myTelNumber,          //	自分側の電話番号
            @QueryParam("recordDate") String recordDate,            //録音時刻
            @QueryParam("startTime") String startTime,              //録音開始日時 （yyyy/MM/dd HH:mm:ss）
            @QueryParam("inOutFlag") String inOutFlag,              //1：着信	0:発信
            @QueryParam("issueCode") String issueCode,              //録音と紐付ける案件のID（省略可）
            @QueryParam("mainRecovoiceID") String mainRecovoiceID   //紐付ける主録音のID（省略可）
        ){
        Map<String, String> entity = new HashMap<>();
        entity.put("RecovoiceID", "1394");
        return Response.ok().entity(new Gson().toJson(entity)).build();
    }
    @GET
    @Path("FinishHistory")
    public Response finishHistory(
            @QueryParam("recovoiceID") String recovoiceID,
            @QueryParam("recorvoiceID") String recorvoiceID,	//録音ID（通話開始時のAddHistoryに対して返って来たID）;;;;;;;
            @QueryParam("finishTime") String finishTime,        //通話終了時刻 （yyyy/MM/dd HH:mm:ss）
            @QueryParam("uniqueID") String uniqueID             //CTI制御が発行する通話の識別ID
        ){
        
        recovoiceID = StringUtils.defaultIfEmpty(recovoiceID, recorvoiceID);
        
        String serverAddress = "ctstageserver"; //get from config
        
        String recordDownloadURL = String.format("http://%s/CtRec2/WebSv/RecFileCtlDownload.aspx?FileUniqueID=%s", serverAddress, uniqueID);
        
        //SAVE record file
        
        return Response.ok().entity("Finish History: " + recovoiceID).build();
    }
    @GET
    @Path("Play")
    public Response play(
            @QueryParam("recovoiceID") String recovoiceID         //録音ID
        ){
        Voice bean = new Voice();
        
        return Response.ok().entity(new Gson().toJson(bean)).build();
    }
    
    @GET
    @Path("StartItelpy")
    public Response startItelpy(@Context HttpServletRequest req,
            @QueryParam("IssueCode") String IssueCode, //案件番号
            @QueryParam("TelNumber") String TelNumber //発信先電話番号欄にセット
    ) throws SocketException {
        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = req.getRemoteAddr();
        }
        int port = 2432;
        DatagramSocket clientSocket = new DatagramSocket();
        try {
            byte[] sendData = new byte[1024];
            Itelpy bean = new Itelpy();
            bean.setTelNumber(TelNumber);
            bean.setIssueCode(IssueCode);
            
            bean.setFirstNameKana("CCC");
            bean.setLastNameKana("AAA");
            bean.setFirstNameKanji("DDD");
            bean.setLastNameKanji("BBB");            
            sendData = new Gson().toJson(bean).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAddress), port);
            clientSocket.send(sendPacket);

        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            clientSocket.close();
        }
        
        return Response.ok(String.format("Start Itelpy AT %s:%d", ipAddress, port)).build();
    }
}
