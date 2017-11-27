package gnext.utils;

import java.io.Serializable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Sử dụng lớp này trong tất cả các công việc mã hóa dữ liệu người dùng.
 * @author daind
 */
public class EncoderUtil implements PasswordEncoder, Serializable {
    private static final long serialVersionUID = 3317430054320950186L;
    private static final PasswordEncoder passEncoder = new BCryptPasswordEncoder();
    
    private EncoderUtil(){}
    
    /**
     * Sử dụng bộ mã hóa trong Spring Framework.
     * @return 
     */
    public static EncoderUtil getPassEncoder() {
        return new EncoderUtil();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword) || passEncoder.matches(rawPassword, encodedPassword);
    }
}
