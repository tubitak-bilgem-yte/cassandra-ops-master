package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util;

public class RandomStringUtil {
    private static String alphaNumeric = "abcdefghjklmnprstuwxyz123456789";

    public static String generateAlphanumericString(final int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int randIndex = (int) (Math.random() * RandomStringUtil.alphaNumeric.length());
            sb.append(RandomStringUtil.alphaNumeric.charAt(randIndex));
        }
        return sb.toString();
    }
}
