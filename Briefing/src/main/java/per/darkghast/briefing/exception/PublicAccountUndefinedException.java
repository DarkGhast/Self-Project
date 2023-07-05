package per.darkghast.briefing.exception;

/**
 * 公众号状态异常
 *
 * @author Dark_Ghast
 */
public class PublicAccountUndefinedException extends RuntimeException {
    public PublicAccountUndefinedException(String message) {
        super(message);
    }
}
