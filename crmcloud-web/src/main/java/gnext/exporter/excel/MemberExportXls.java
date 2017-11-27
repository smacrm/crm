/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import gnext.bean.CompanyTargetInfo;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.Prefecture;
import gnext.controller.company.MemberController;
import gnext.model.authority.UserModel;
import gnext.model.company.member.MemberModel;
import gnext.service.MemberService;
import gnext.service.PrefectureService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author tungdt
 */
public class MemberExportXls extends AbstractExportXls {
    private final MemberService memberService;
    private final PrefectureService prefectureService;
    private final MemberController memberController;
    private final List<Member> members;
    private final List<MemberModel> memberModels;
    
    public MemberExportXls(MemberController memberController, String fileName) {
        super(fileName);
        this.memberController = memberController;
        this.memberService = this.memberController.getMemberService();
        this.prefectureService = this.memberController.getPrefectureService();
        this.members = memberService.findByCompanyId(UserModel.getLogined().getCompanyId());
        this.memberModels = new ArrayList<>();
    }

    @Override
    protected void fillHeader(Workbook workbook, Sheet sheet) throws Exception {
        String[] headers = {msgBundle.getString("label.loginId"), msgBundle.getString("label.password"), companyBundle.getString("label.member.name.kanji"), 
                            companyBundle.getString("label.member.name.kana"), companyBundle.getString("label.member.code"), 
                            msgBundle.getString("label.zipcode"), msgBundle.getString("label.state"), msgBundle.getString("label.address"), 
                            msgBundle.getString("label.address.kana"), msgBundle.getString("label.phone"), msgBundle.getString("label.mobile.phone"), msgBundle.getString("label.company.email")
                            , companyBundle.getString("label.member.administrator"), msgBundle.getString("label.memo")};
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            String memberLoginId = member.getMemberLoginId();
            if (memberLoginId.length() > 5) {
                String memberLoginIdSub = memberLoginId.substring(5, memberLoginId.length());
                member.setMemberLoginId(memberLoginIdSub);
            }
            MemberModel model = new MemberModel(member);
            model.updateExtraInfors(memberController);
            model.initInfos(memberController.getCompanyTargetInfoService());
            memberModels.add(model);
        }

        List<String> listHeader = new ArrayList<>(Arrays.asList(headers));
        int maxGroup = findMaxNumberOfGroup(memberModels);
        for (int i = 1; i <= maxGroup; i++) {
            listHeader.add(msgBundle.getString("label.group.name") + i);
        }
        
        for (int i = 0; i < listHeader.size(); i++) createCell(headerRow, i, listHeader.get(i), createHeaderCellStyle(workbook));
    }

    @Override
    protected void fillBody(Workbook workbook, Sheet sheet) throws Exception {
        CellStyle cellStyle = creatBodyCellStyle(workbook);

        int aCurrentRow = 1;
        for (MemberModel data : memberModels) {
            Row aRow = sheet.createRow(aCurrentRow);
            // cell id
            createCell(aRow, 0, data.getMember().getMemberLoginId(), cellStyle);
            // cell password
            createCell(aRow, 1, "", cellStyle);
            //cell name kanji
            createCell(aRow, 2, data.getMember().getMemberNameFirst() == null ? "" : data.getMember().getMemberNameFirst(), cellStyle);
            // cell name kana
            createCell(aRow, 3, data.getMember().getMemberKanaFirst() == null ? "" : data.getMember().getMemberKanaFirst(), cellStyle);
            // cell member code
            createCell(aRow, 4, data.getMember().getMemberCode(), cellStyle);
            createCell(aRow, 5, data.getMember().getMemberPost(), cellStyle);
            Prefecture prefecture = prefectureService.findByPrefectureCode(memberController.getLocale(),
                    String.valueOf(data.getMember().getMemberCity()));
            createCell(aRow, 6, prefecture == null ? "" : String.valueOf(prefecture.getPrefectureName()), cellStyle);
            createCell(aRow, 7, data.getMember().getMemberAddress() == null ? "" : String.valueOf(data.getMember().getMemberAddress()), cellStyle);
            createCell(aRow, 8, data.getMember().getMemberAddressKana() == null ? "" : String.valueOf(data.getMember().getMemberAddressKana()), cellStyle);
            createCell(aRow, 9, buildCompanyTargetInfoToExport(data.getMemberPhones()), cellStyle);
            createCell(aRow, 10, buildCompanyTargetInfoToExport(data.getMemberMobilePhones()), cellStyle);
            createCell(aRow, 11, buildCompanyTargetInfoToExport(data.getMemberEmails()), cellStyle);
            if (data.getMember().getMemberManagerFlag() != null) {
                if ((short) 1 == data.getMember().getMemberManagerFlag()) {
                    createCell(aRow, 12, companyBundle.getString("label.member.manager"), cellStyle);
                } else {
                    createCell(aRow, 12, companyBundle.getString("label.member.general"), cellStyle);
                }
            } else createCell(aRow, 12, companyBundle.getString("label.member.general"), cellStyle);
            createCell(aRow, 13, data.getMember().getMemberMemo(), cellStyle);

            Stack<String> groupParentName = new Stack<>();
            if (data.getMember().getGroup() != null) {
                groupParentName.add(data.getMember().getGroup().getGroupName());
            }
            groupParentName = getGroupParentName(data.getMember().getGroup(), groupParentName);
            int groupParentSize = groupParentName.size();
            int maxGroup = findMaxNumberOfGroup(memberModels);
            for (int i = 1; i <= maxGroup; i++) {
                Cell cellGroup = aRow.createCell(13 + i);
                cellGroup.setCellStyle(cellStyle);

                if (groupParentSize >= i) {
                    cellGroup.setCellValue(groupParentName.pop());
                } else {
                    cellGroup.setCellValue("");
                }
            }

            aCurrentRow++;
        }
    }

    @Override
    protected void fillFooter(Workbook workbook, Sheet sheet) throws Exception {
        //To change body of generated methods, choose Tools | Templates.
    }

    ///////////// PRIVATE METHODS /////////////
    private CellStyle creatBodyCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }
    
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();

        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        return headerStyle;
    }
    
    private Integer findMaxNumberOfGroup(List<MemberModel> models) {
        int max = 0;
        for (MemberModel model : models) {
            Stack<String> groups = new Stack<>();
            if (model.getMember().getGroup() != null) {
                groups.add(model.getMember().getGroup().getGroupName());
            }
            groups = getGroupParentName(model.getMember().getGroup(), groups);
            if (groups.size() > max) {
                max = groups.size();
            }
        }
        return max;
    }

    private Stack<String> getGroupParentName(Group group, Stack<String> listGroupParent) {
        if (group.getParent() != null) {
            listGroupParent.add(String.valueOf(group.getParent().getGroupName()));
            group = group.getParent();
            return getGroupParentName(group, listGroupParent);
        } else {
            return listGroupParent;
        }
    }

    private String buildCompanyTargetInfoToExport(List<CompanyTargetInfo> datas) {
        StringBuilder data = new StringBuilder();
        String prefix = "";
        for (int i = 0; i < datas.size(); i++) {
            data.append(prefix);
            prefix = ",";
            data.append(datas.get(i).getCompanyTargetData());
        }
        return data.toString();
    }

}
