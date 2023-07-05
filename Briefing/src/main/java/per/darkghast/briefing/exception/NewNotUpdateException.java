package per.darkghast.briefing.exception;

/**
 * 新闻未更新
 *
 * @author Dark_Ghast
 */
public class NewNotUpdateException extends RuntimeException {
    public NewNotUpdateException(String message) {
        super(message);
    }
}
