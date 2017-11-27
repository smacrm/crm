/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer.excel;

import gnext.bean.CompanyTargetInfo;
import gnext.bean.CompanyTargetInfoPK;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.Prefecture;
import gnext.controller.company.MemberController;
import gnext.exporter.excel.MemberExportXls;
import gnext.importer.Import;
import gnext.model.authority.UserModel;
import gnext.model.company.member.MemberModel;
import gnext.service.GroupService;
import gnext.service.MemberImportService;
import gnext.service.PrefectureService;
import gnext.util.JsfUtil;
import gnext.util.StringUtil;
import gnext.validator.BaseValidator;
import gnext.validator.PhoneFaxValidator;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tungdt
 */
public class MemberImport implements Import {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberExportXls.class);
    private MemberImportService memberImportService;
    private PrefectureService prefectureService;
    private MemberController memberController;
    private GroupService groupService;
    private List<MemberModel> memberModels;

    public MemberImport() {

    }

    public MemberImport(MemberController memberController) {
        this.memberController = memberController;
        this.memberImportService = this.memberController.getMemberImportService();
        this.prefectureService = this.memberController.getPrefectureService();
        this.groupService = this.memberController.getGroupService();
        this.memberModels = new ArrayList<>();
    }

    @Override
    public void execute(InputStream is) throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook(is);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow headerRow = sheet.getRow(0);
        int numberOfCell = headerRow.getLastCellNum();
        Set<String> setMemberName = new HashSet<String>();
        for (int i = 1; i <= sheet.getPhysicalNumberOfRows(); i++) {
            int currentSizeSet = setMemberName.size();
            HSSFRow row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Member mMember = new Member();
            MemberModel mMemberModel = new MemberModel();

            Cell memberLoginIdCell = row.getCell(0);
            String memberLoginId = getCellValue(memberLoginIdCell);
            setMemberName.add(memberLoginId);
            if (currentSizeSet == setMemberName.size()) {
                String message = "Duplicate Member Login Id At" + " Column " + getCellName(memberLoginIdCell);
                JsfUtil.addErrorMessage(message);
                return;
            }
            memberLoginId = StringUtil.generatePrefixLoginId(memberLoginId, UserModel.getLogined().getCompanyId());
            if (!requireCellValidator(memberLoginId, memberLoginIdCell, "Member Login Id Cell Cannot Null")) {
                return;
            }
            if (!lengthCellValidator(memberLoginId, memberLoginIdCell, 70, "Member Login Id Cell Length Cannot Greater Than " + 70)) {
                return;
            }
            mMember.setMemberLoginId(memberLoginId);

            Cell groupNameCell = row.getCell(14);
            String groupName = getCellValue(groupNameCell);
            if (!requireCellValidator(groupName, groupNameCell, "Group Name Cell Cannot Null")) {
                return;
            }
            List<String> listGroupName = new ArrayList<>();
            for (int j = 14; j < numberOfCell; j++) {
                Cell mGroupNameCell = row.getCell(j);
                if (mGroupNameCell == null || getCellValue(mGroupNameCell).isEmpty()) {
                    break;
                }
                listGroupName.add(getCellValue(mGroupNameCell));
            }
            mMember.setListGroupName(listGroupName);
            int listGroupNameLength = listGroupName.size();
            String memberGroupName = listGroupName.get(listGroupNameLength - 1);
            StringBuilder sbGroupName = new StringBuilder();
            String prefix = "";
            for (int k = 0; k < listGroupNameLength; k++) {
                sbGroupName.append(prefix);
                sbGroupName.append(listGroupName.get(k));
                prefix = ",";
            }

            List<Group> listGroup = groupService.findByGroupName(memberGroupName, UserModel.getLogined().getCompanyId());
            Group group = memberImportService.getMemberGroup(listGroup, sbGroupName.toString());
            mMember.setGroup(group);

            if (!convertMemberData(row, mMemberModel, mMember)) {
                return;
            }

            mMember.setCreator(UserModel.getLogined().getMember());
            mMember.setCreatedTime(Calendar.getInstance().getTime());
            mMember.setMemberDeleted((short) 0);

            mMemberModel.setMember(mMember);
            memberModels.add(mMemberModel);
        }
        saveToDatabase();
    }

    private void saveToDatabase() throws Exception {
        if (memberModels.isEmpty()) {
            return;
        }
        Map<Member, List<CompanyTargetInfo>> importData = new HashMap<>();
        int fakeMemberId = -1;
        for (MemberModel memberModel : memberModels) {
            List<CompanyTargetInfo> ctis = getMemberCompanyTargetInfo(memberModel);
            memberModel.getMember().setMemberId(fakeMemberId);
            importData.put(memberModel.getMember(), ctis);
            fakeMemberId--;
        }
        memberImportService.batchUpdate(importData, UserModel.getLogined().getMember());
    }

    private boolean requireCellValidator(String memberLoginId, Cell cell, String message) {
        if (memberLoginId.isEmpty()) {
            message = message + " column " + getCellName(cell);
            JsfUtil.addErrorMessage(message);
            return false;
        }
        return true;
    }

    private boolean lengthCellValidator(String memberLoginId, Cell cell, int max, String message) {
        if (memberLoginId.length() > max) {
            message = message + " column " + getCellName(cell);
            JsfUtil.addErrorMessage(message);
            return false;
        }
        return true;
    }

    private boolean phoneFaxCellValidator(String value, Cell cell, String message) {
        if (value.isEmpty()) {
            return true;
        }
        String[] phoneFaxSplit = value.split(",");
        BaseValidator validator = new PhoneFaxValidator();
        for (String phoneFax : phoneFaxSplit) {
            if(!validator.doValidate(phoneFax)) {
                message = message + " column " + getCellName(cell);
                JsfUtil.addErrorMessage(message);
                return false;
            }
        }
        return true;
    }

    private boolean emailCellValidator(String value, Cell cell, String message) {
        if (value.isEmpty()) {
            return true;
        }
        String[] mailSplit = value.split(",");
        for (String mail : mailSplit) {
            try {
                new InternetAddress(mail).validate();
            } catch (AddressException ex) {
                message = message + " column " + getCellName(cell);
                JsfUtil.addErrorMessage(message);
                return false;
            }
        }
        return true;
    }

    public boolean convertMemberData(Row row, MemberModel mMemberModel, Member mMember) {
        Cell memberPasswordCell = row.getCell(1);
        String memberPassword = getCellValue(memberPasswordCell);
        mMember.setMemberPassword(memberPassword);

        Cell memberNameCell = row.getCell(2);
        String memberName = getCellValue(memberNameCell);
        if (!requireCellValidator(memberName, memberNameCell, "Member Name Kanji Cell Cannot Null")) {
            return false;
        }
        mMember.setMemberNameFirst(memberName);
        mMember.setMemberNameLast(" ");

        Cell memberKanaCell = row.getCell(3);
        String memberKana = getCellValue(memberKanaCell);
        mMember.setMemberKanaFirst(memberKana);

        Cell memberCodeCell = row.getCell(4);
        memberCodeCell.setCellType(CellType.STRING);
        String memberCode = getCellValue(memberCodeCell);
        mMember.setMemberCode(memberCode);

        Cell memberPostCell = row.getCell(5);
        memberPostCell.setCellType(CellType.STRING);
        String memberPost = getCellValue(memberPostCell);
        if (!phoneFaxCellValidator(memberPost, memberPostCell, "Post Is Wrong Format")) {
            return false;
        }
        mMember.setMemberPost(memberPost);

        Cell memberPrefectureCell = row.getCell(6);
        String memberPrefecture = getCellValue(memberPrefectureCell);
        Prefecture prefecture = prefectureService.findByPrefectureName(memberController.getLocale(), memberPrefecture);
        if (prefecture != null) {
            mMember.setMemberCity(Integer.parseInt(prefecture.getPrefectureCode()));
        }

        Cell memberAddressCell = row.getCell(7);
        String memberAddress = getCellValue(memberAddressCell);
        mMember.setMemberAddress(memberAddress);

        Cell memberAddressKanaCell = row.getCell(8);
        String memberAddressKana = getCellValue(memberAddressKanaCell);
        mMember.setMemberAddressKana(memberAddressKana);

        Cell memberPhoneCell = row.getCell(9);
        memberPhoneCell.setCellType(CellType.STRING);
        String memberPhone = getCellValue(memberPhoneCell);
        if (!phoneFaxCellValidator(memberPhone, memberPhoneCell, "Phone Is Wrong Format")) {
            return false;
        }
        if (!memberPhone.isEmpty()) {
            buildCompanyTargetInfoToImport(memberPhone, mMemberModel.getMemberPhones(), "PHONE");
        }

        Cell memberMobilePhoneCell = row.getCell(10);
        memberMobilePhoneCell.setCellType(CellType.STRING);
        String memberMobilePhone = getCellValue(memberMobilePhoneCell);
        if (!phoneFaxCellValidator(memberMobilePhone, memberMobilePhoneCell, "Mobile Phone Is Wrong Format")) {
            return false;
        }
        if (!memberMobilePhone.isEmpty()) {
            buildCompanyTargetInfoToImport(memberMobilePhone, mMemberModel.getMemberMobilePhones(), "MOBILE");
        }

        Cell memberEmailCell = row.getCell(11);
        String memberEmail = getCellValue(memberEmailCell);
        if (!emailCellValidator(memberEmail, memberEmailCell, "Email Is Wrong Format")) {
            return false;
        }
        if (!memberEmail.isEmpty()) {
            buildCompanyTargetInfoToImport(memberEmail, mMemberModel.getMemberEmails(), "EMAIL");
        }

        Cell memberManagerFlagCell = row.getCell(12);
        String memberManagerFlag = getCellValue(memberManagerFlagCell);
        if ("管理者".equals(memberManagerFlag)) {
            mMember.setMemberManagerFlag((short) 0);
        } else {
            mMember.setMemberManagerFlag((short) 1);
        }

        Cell memberMemoCell = row.getCell(13);
        String memberMemo = getCellValue(memberMemoCell);
        mMember.setMemberMemo(memberMemo);

        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell != null) {
            return cell.toString();
        }
        return "";
    }

    private String getCellName(Cell cell) {
        return CellReference.convertNumToColString(cell.getColumnIndex()) + (cell.getRowIndex() + 1);
    }

    private void buildCompanyTargetInfoToImport(String companyTargetData, List<CompanyTargetInfo> infos, String type) {
        String[] companyTargetDataSplits = companyTargetData.split(",");
        for (String companyTargetDataSplit : companyTargetDataSplits) {
            CompanyTargetInfoPK companyTargetInfoPK = new CompanyTargetInfoPK();
            companyTargetInfoPK.setCompanyTarget(CompanyTargetInfo.COMPANY_TARGET_MEMBER);
            if ("PHONE".equals(type)) {
                companyTargetInfoPK.setCompanyFlagType(CompanyTargetInfo.COMPANY_FLAG_TYPE_PHONE);
                CompanyTargetInfo info = new CompanyTargetInfo(companyTargetInfoPK);
                info.setCompanyTargetData(companyTargetDataSplit);
                infos.add(info);
            } else if ("MOBILE".equals(type)) {
                CompanyTargetInfo info = new CompanyTargetInfo(companyTargetInfoPK);
                info.getCompanyTargetInfoPK().setCompanyFlagType(CompanyTargetInfo.COMPANY_FLAG_TYPE_MOBILE);
                info.setCompanyTargetData(companyTargetDataSplit);
                infos.add(info);
            } else if ("EMAIL".equals(type)) {
                CompanyTargetInfo info = new CompanyTargetInfo(companyTargetInfoPK);
                info.getCompanyTargetInfoPK().setCompanyFlagType(CompanyTargetInfo.COMPANY_FLAG_TYPE_EMAIL);
                info.setCompanyTargetData(companyTargetDataSplit);
                infos.add(info);
            }
        }
    }

    private List<CompanyTargetInfo> getMemberCompanyTargetInfo(MemberModel mMemberModel) {
        List<CompanyTargetInfo> ctis = new ArrayList<>();
        mMemberModel.getMemberPhones().forEach((memberPhone) -> {
            ctis.add(memberPhone);
        });
        mMemberModel.getMemberMobilePhones().forEach((memberMobilePhone) -> {
            ctis.add(memberMobilePhone);
        });
        mMemberModel.getMemberEmails().forEach((memberPhone) -> {
            ctis.add(memberPhone);
        });
        return ctis;
    }

}
