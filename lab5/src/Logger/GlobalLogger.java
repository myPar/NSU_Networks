package Logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// logger class which can have only two instances - exception logger and workflow logger
public class GlobalLogger {
    public enum LoggerType {EXCEPTION_LOGGER, WORKFLOW_LOGGER};
    public enum Mode {ENABLE, DISABLE}  // logger mode (messages will be logging if mode equals 'ENABLE')

    private Logger logger;
    private Mode mode = Mode.ENABLE;    // logger is enabled by default

    // singleton helper
    public static class LoggerCreator {
        private static GlobalLogger exceptionLogger;
        private static GlobalLogger workflowLogger;

        public static GlobalLogger getLogger(LoggerType type) throws IOException {
            switch (type) {
                case WORKFLOW_LOGGER: {
                    if (workflowLogger == null) {
                        workflowLogger = new GlobalLogger(type);
                    }
                    return workflowLogger;
                }
                case EXCEPTION_LOGGER: {
                    if (exceptionLogger == null) {
                        exceptionLogger = new GlobalLogger(type);
                    }
                    return exceptionLogger;
                }
                default:
                    assert false;
            }
            return null;
        }
    }
    private GlobalLogger(LoggerType type) throws IOException {
        switch (type) {
            case EXCEPTION_LOGGER: {
                logger = Logger.getLogger("ExceptionLogger");
                logger.setUseParentHandlers(false);
                logger.addHandler(new FileHandler("exceptions_%g.log"));
                break;
            }
            case WORKFLOW_LOGGER: {
                logger = Logger.getLogger("WorkflowLogger");
                logger.setUseParentHandlers(false);
                logger.addHandler(new ConsoleHandler());
                break;
            }
            default:
                assert false;
        }
    }
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    public void log(Level level, String msg) {
        if (this.mode == Mode.ENABLE) {
            logger.log(level, msg);
        }
    }
}
