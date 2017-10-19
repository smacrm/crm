package gnext.service.attachment;

import gnext.bean.attachment.Attachment;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface AttachmentService extends EntityService<Attachment> {
    public Attachment search(Integer companyId, Integer attchmentId);
    public List<Attachment> search(Integer companyId, Integer attachmentTargetType, Integer attachmentTargetId, Short attachmentDeleted);
    public int deleteAttachmentByEscalationId(Integer escId);
    public void deleteAttachment(int attachmentId);
}
