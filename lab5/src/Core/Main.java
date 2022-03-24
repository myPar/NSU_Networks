package Core;

import Logger.GlobalLogger;

import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        GlobalLogger workflowLogger;
        GlobalLogger exceptionLogger;

        workflowLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.WORKFLOW_LOGGER);
        exceptionLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.EXCEPTION_LOGGER);

        assert workflowLogger != null;
        workflowLogger.log(Level.INFO, "message1");

        assert exceptionLogger != null;
        exceptionLogger.log(Level.INFO, "message22");
    }

}
