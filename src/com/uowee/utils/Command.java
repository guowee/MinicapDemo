package com.uowee.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Command {
    public Command() {

    }

    public CommandResult executeCommand(String command, String filter) {
        Logger.debug("EXECUTE COMMAND:" + command);
        int result = -1;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        String[] cmd = new String[3];
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows")) {
            cmd[0] = "cmd.exe";
            cmd[1] = "/c";
            cmd[2] = command;
        } else {
            cmd[0] = "bash";
            cmd[1] = "-c";
            cmd[2] = command;
        }
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmd);
        InputStream is1 = null;
        InputStream is2 = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        Process p = null;
        try {
            p = builder.start();
            is1 = p.getInputStream();
            is2 = p.getErrorStream();
            successResult = new BufferedReader(new InputStreamReader(is1, "UTF-8"));
            errorResult = new BufferedReader(new InputStreamReader(is2, "UTF-8"));
            String line1;
            while ((line1 = successResult.readLine()) != null) {
                if (filter == null) {
                    successMsg.append(line1);
                } else {
                    if (line1.contains(filter)) {
                        successMsg.append(line1);
                    }
                }
            }

            while ((line1 = errorResult.readLine()) != null) {
                errorMsg.append(line1);
            }
            result = p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                is1.close();
                is2.close();
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
                p.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }


    public static class CommandResult {

        /**
         * result of command
         **/
        public int result;
        /**
         * success message of command result
         **/
        public String successMsg;
        /**
         * error message of command result
         **/
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }

}
