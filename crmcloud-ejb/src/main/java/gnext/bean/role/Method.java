package gnext.bean.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_method", uniqueConstraints = @UniqueConstraint(columnNames = "method_id"))
@XmlRootElement
public class Method implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "method_id")
    @Setter @Getter
    private Integer methodId;
    
    @Basic(optional = false)
    @Size(min = 1, max = 255)
    @Column(name = "method_name", unique = true, nullable = false)
    @Setter @Getter
    private String methodName;
    
    @Column(name = "method_order")
    @Setter @Getter
    private Integer methodOrder = 100;
    
    @Column(name = "creator_id")
    @Setter @Getter
    private Integer creatorId;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Setter @Getter
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date updatedTime;
    
    @ManyToMany(mappedBy = "methodList")
    @Setter @Getter
    private List<Page> pageList = new ArrayList<>();

    public Method() {
    }

    public Method(Integer methodId) {
        this.methodId = methodId;
    }

    public Method(Integer methodId, String methodName) {
        this.methodId = methodId;
        this.methodName = methodName;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (methodId != null ? methodId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Method)) {
            return false;
        }
        Method other = (Method) object;
        if ((this.methodId == null && other.methodId != null) || (this.methodId != null && !this.methodId.equals(other.methodId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.bean.a.Method[ methodId=" + methodId + " ]";
    }
    
}
