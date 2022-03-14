package Core;

import Logger.GlobalLogger;

import java.io.IOException;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        GlobalLogger workflowLogger;
        GlobalLogger exceptionLogger;
        try {
            workflowLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.WORKFLOW_LOGGER);
            exceptionLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.EXCEPTION_LOGGER);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        assert workflowLogger != null;
        workflowLogger.log(Level.INFO, "message1");

        assert exceptionLogger != null;
        //exceptionLogger.log(Level.INFO, "message22");
    }

}
