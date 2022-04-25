package util;

import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;

public class KerberosAuth1 {
    public static void kerberosAuth(Boolean debug){
        try {
//            File krb5_file = new File("/data/flink/krb5.conf");
//            File keytab_file = new File("/data/flink/bonc-lvpt-01-zongshuai.keytab");

            File krb5_file = new File("E:\\bonc_src\\flink14cdc\\src\\main\\resources\\krb5.conf");
            File keytab_file = new File("E:\\bonc_src\\flink14cdc\\src\\main\\resources\\bonc-lvpt-01-zongshuai.keytab");

            System.setProperty("java.security.krb5.conf",  krb5_file.getAbsolutePath());
            System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
            if (debug) {
                System.setProperty("sun.security.krb5.debug", "true");
            }
            UserGroupInformation.loginUserFromKeytab("bonc/lvpt-01-zongshuai@BXCDH.COM", keytab_file.getAbsolutePath());
            System.out.println(UserGroupInformation.getCurrentUser());
        } catch (Exception e){
            e.getStackTrace();
        }
    }
}
