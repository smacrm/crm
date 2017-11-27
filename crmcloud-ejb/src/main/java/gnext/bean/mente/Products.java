package gnext.bean.mente;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Jan 12, 2017
 */
@Entity
@Table(name = "crm_products")
@XmlRootElement
@NamedQueries({
     @NamedQuery(name = "Products.findProductNameById", query = "SELECT c FROM Products c WHERE c.productsId = :productsId ")
})
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "products_id")
    @Getter @Setter
    private Integer productsId;

    @Column(name = "products_category_small_id")
    @Getter @Setter
    private Integer productsCategorySmallId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "products_code")
    @Getter @Setter
    private String productsCode;

    @Size(max = 64)
    @Column(name = "products_real_code")
    @Getter @Setter
    private String productsRealCode;

    @Size(max = 64)
    @Column(name = "products_itf_code")
    @Getter @Setter
    private String productsItfCode;

    @Size(max = 64)
    @Column(name = "products_jan_code")
    @Getter @Setter
    private String productsJanCode;

    @Size(max = 256)
    @Column(name = "products_name")
    @Getter @Setter
    private String productsName;

    @Size(max = 256)
    @Column(name = "products_name_search")
    @Getter @Setter
    private String productsNameSearch;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_description")
    @Getter @Setter
    private String productsDescription;

    @Size(max = 128)
    @Column(name = "products_price")
    @Getter @Setter
    private String productsPrice;

    @Size(max = 128)
    @Column(name = "products_dept_in_charge")
    @Getter @Setter
    private String productsDeptInCharge;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_provider")
    @Getter @Setter
    private String productsProvider;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_factory")
    @Getter @Setter
    private String productsFactory;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_package_material")
    @Getter @Setter
    private String productsPackageMaterial;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_package_note")
    @Getter @Setter
    private String productsPackageNote;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_package_size")
    @Getter @Setter
    private String productsPackageSize;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_picture")
    @Getter @Setter
    private String productsPicture;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_picture_encode")
    @Getter @Setter
    private String productsPictureEncode;

    @Size(max = 128)
    @Column(name = "products_capacity")
    @Getter @Setter
    private String productsCapacity;

    @Size(max = 128)
    @Column(name = "products_process_method")
    @Getter @Setter
    private String productsProcessMethod;

    @Size(max = 128)
    @Column(name = "products_type_package")
    @Getter @Setter
    private String productsTypePackage;

    @Size(max = 128)
    @Column(name = "products_details_type_name")
    @Getter @Setter
    private String productsDetailsTypeName;

    @Size(max = 128)
    @Column(name = "products_details_modify_gen")
    @Getter @Setter
    private String productsDetailsModifyGen;

    @Size(max = 16)
    @Column(name = "products_details_calcium")
    @Getter @Setter
    private String productsDetailsCalcium;

    @Size(max = 16)
    @Column(name = "products_details_gluxit")
    @Getter @Setter
    private String productsDetailsGluxit;

    @Size(max = 16)
    @Column(name = "products_details_salt")
    @Getter @Setter
    private String productsDetailsSalt;

    @Size(max = 16)
    @Column(name = "products_details_natri")
    @Getter @Setter
    private String productsDetailsNatri;

    @Size(max = 16)
    @Column(name = "products_details_lipid")
    @Getter @Setter
    private String productsDetailsLipid;

    @Size(max = 16)
    @Column(name = "products_details_protein")
    @Getter @Setter
    private String productsDetailsProtein;

    @Size(max = 16)
    @Column(name = "products_details_energy")
    @Getter @Setter
    private String productsDetailsEnergy;

    @Size(max = 128)
    @Column(name = "products_details_alcohol_name")
    @Getter @Setter
    private String productsDetailsAlcoholName;

    @Size(max = 128)
    @Column(name = "products_details_caffeine")
    @Getter @Setter
    private String productsDetailsCaffeine;

    @Size(max = 128)
    @Column(name = "products_details_additives")
    @Getter @Setter
    private String productsDetailsAdditives;

    @Size(max = 128)
    @Column(name = "products_details_material_made_position")
    @Getter @Setter
    private String productsDetailsMaterialMadePosition;

    @Size(max = 128)
    @Column(name = "products_details_material_name")
    @Getter @Setter
    private String productsDetailsMaterialName;

    @Size(max = 128)
    @Column(name = "products_details_component")
    @Getter @Setter
    private String productsDetailsComponent;

    @Size(max = 128)
    @Column(name = "products_details_unit_display")
    @Getter @Setter
    private String productsDetailsUnitDisplay;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_details_note")
    @Getter @Setter
    private String productsDetailsNote;

    @Size(max = 16)
    @Column(name = "products_details_other_component_8_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent8Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_8_name")
    @Getter @Setter
    private String productsDetailsOtherComponent8Name;

    @Size(max = 16)
    @Column(name = "products_details_other_component_7_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent7Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_7_name")
    @Getter @Setter
    private String productsDetailsOtherComponent7Name;

    @Size(max = 16)
    @Column(name = "products_details_other_component_6_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent6Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_6_name")
    @Getter @Setter
    private String productsDetailsOtherComponent6Name;

    @Size(max = 16)
    @Column(name = "products_details_other_component_5_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent5Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_5_name")
    @Getter @Setter
    private String productsDetailsOtherComponent5Name;

    @Size(max = 16)
    @Column(name = "products_details_other_component_4_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent4Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_4_name")
    @Getter @Setter
    private String productsDetailsOtherComponent4Name;

    @Size(max = 16)
    @Column(name = "products_details_other_component_3_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent3Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_3_name")
    @Getter @Setter
    private String productsDetailsOtherComponent3Name;

    @Size(max = 16)
    @Column(name = "products_details_other_component_2_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent2Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_2_name")
    @Getter @Setter
    private String productsDetailsOtherComponent2Name;

    @Size(max = 16)
    @Column(name = "products_details_other_component_1_percent")
    @Getter @Setter
    private String productsDetailsOtherComponent1Percent;

    @Size(max = 32)
    @Column(name = "products_details_other_component_1_name")
    @Getter @Setter
    private String productsDetailsOtherComponent1Name;

    @Size(max = 128)
    @Column(name = "products_self_management_component")
    @Getter @Setter
    private String productsSelfManagementComponent;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_self_management_note")
    @Getter @Setter
    private String productsSelfManagementNote;

    @Size(max = 128)
    @Column(name = "products_other_size")
    @Getter @Setter
    private String productsOtherSize;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_other")
    @Getter @Setter
    private String productsOther;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_memo")
    @Getter @Setter
    private String productsMemo;

    @Lob
    @Size(max = 65535)
    @Column(name = "products_sales_catch")
    @Getter @Setter
    private String productsSalesCatch;

    @Column(name = "products_allergen_flag_wheat")
    @Getter @Setter
    private Boolean productsAllergenFlagWheat;

    @Column(name = "products_allergen_flag_soba")
    @Getter @Setter
    private Boolean productsAllergenFlagSoba;

    @Column(name = "products_allergen_flag_egg")
    @Getter @Setter
    private Boolean productsAllergenFlagEgg;

    @Column(name = "products_allergen_flag_milk")
    @Getter @Setter
    private Boolean productsAllergenFlagMilk;

    @Column(name = "products_allergen_flag_nuts")
    @Getter @Setter
    private Boolean productsAllergenFlagNuts;

    @Column(name = "products_allergen_flag_abalone")
    @Getter @Setter
    private Boolean productsAllergenFlagAbalone;

    @Column(name = "products_allergen_flag_squid")
    @Getter @Setter
    private Boolean productsAllergenFlagSquid;

    @Column(name = "products_allergen_flag_ikura")
    @Getter @Setter
    private Boolean productsAllergenFlagIkura;

    @Column(name = "products_allergen_flag_shrimp")
    @Getter @Setter
    private Boolean productsAllergenFlagShrimp;

    @Column(name = "products_allergen_flag_orange")
    @Getter @Setter
    private Boolean productsAllergenFlagOrange;

    @Column(name = "products_allergen_flag_crab")
    @Getter @Setter
    private Boolean productsAllergenFlagCrab;

    @Column(name = "products_allergen_flag_kiwi")
    @Getter @Setter
    private Boolean productsAllergenFlagKiwi;

    @Column(name = "products_allergen_flag_beef")
    @Getter @Setter
    private Boolean productsAllergenFlagBeef;

    @Column(name = "products_allergen_flag_kurumi")
    @Getter @Setter
    private Boolean productsAllergenFlagKurumi;

    @Column(name = "products_allergen_flag_sake")
    @Getter @Setter
    private Boolean productsAllergenFlagSake;

    @Column(name = "products_allergen_flag_mackerel")
    @Getter @Setter
    private Boolean productsAllergenFlagMackerel;

    @Column(name = "products_allergen_flag_soy")
    @Getter @Setter
    private Boolean productsAllergenFlagSoy;

    @Column(name = "products_allergen_flag_chicken")
    @Getter @Setter
    private Boolean productsAllergenFlagChicken;

    @Column(name = "products_allergen_flag_pork")
    @Getter @Setter
    private Boolean productsAllergenFlagPork;

    @Column(name = "products_allergen_flag_matsutake")
    @Getter @Setter
    private Boolean productsAllergenFlagMatsutake;

    @Column(name = "products_allergen_flag_yam")
    @Getter @Setter
    private Boolean productsAllergenFlagYam;

    @Column(name = "products_allergen_flag_taro")
    @Getter @Setter
    private Boolean productsAllergenFlagTaro;

    @Column(name = "products_allergen_flag_apple")
    @Getter @Setter
    private Boolean productsAllergenFlagApple;

    @Column(name = "products_allergen_flag_gelatin")
    @Getter @Setter
    private Boolean productsAllergenFlagGelatin;

    @Column(name = "products_allergen_flag_banana")
    @Getter @Setter
    private Boolean productsAllergenFlagBanana;

    @Column(name = "products_allergen_flag_sesame")
    @Getter @Setter
    private Boolean productsAllergenFlagSesame;

    @Column(name = "products_allergen_flag_cashew_nuts")
    @Getter @Setter
    private Boolean productsAllergenFlagCashewNuts;

    @Size(max = 128)
    @Column(name = "products_allergen_contamination")
    @Getter @Setter
    private String productsAllergenContamination;

    @Column(name = "products_public_start_day")
    @Temporal(TemporalType.DATE)
    @Getter @Setter
    private Date productsPublicStartDay;

    @Column(name = "products_public_end_day")
    @Temporal(TemporalType.DATE)
    @Getter @Setter
    private Date productsPublicEndDay;

    @Column(name = "products_sale_start_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date productsSaleStartDate;

    @Column(name = "products_sale_end_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date productsSaleEndDate;

    @Size(max = 128)
    @Column(name = "products_expiration_date")
    @Getter @Setter
    private String productsExpirationDate;

    @Size(max = 128)
    @Column(name = "products_expiration_position")
    @Getter @Setter
    private String productsExpirationPosition;

    @Basic(optional = false)
    @NotNull
    @Column(name = "products_order")
    @Getter @Setter
    private int productsOrder = 1;

    @Column(name = "products_is_public")
    @Getter @Setter
    private Boolean productsIsPublic;

    @Basic(optional = false)
    @NotNull
    @Column(name = "products_is_deleted")
    @Getter @Setter
    private boolean productsIsDeleted;

    @Column(name = "products_creator_id")
    @Getter @Setter
    private Integer productsCreatorId;

    @Column(name = "products_created_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date productsCreatedDatetime;

    @Column(name = "products_updater_id")
    @Getter @Setter
    private Integer productsUpdaterId;

    @Column(name = "products_updated_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date productsUpdatedDatetime;

    @Column(name = "company_id")
    @Getter @Setter
    private Integer companyId;
    
    @Transient
    @Getter @Setter
    private String itemName = "issue_product_id";

    public Products() {
    }

    public Products(Integer productsId) {
        this.productsId = productsId;
    }

    public Products(Integer productsId, String productsCode, int productsOrder, boolean productsIsDeleted) {
        this.productsId = productsId;
        this.productsCode = productsCode;
        this.productsOrder = productsOrder;
        this.productsIsDeleted = productsIsDeleted;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (productsId != null ? productsId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Products)) {
            return false;
        }
        Products other = (Products) object;
        if ((this.productsId == null && other.productsId != null) || (this.productsId != null && !this.productsId.equals(other.productsId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return productsId.toString();
    }

}
