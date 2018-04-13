package pn150121d.kdp.stockmarket.common;

/**
 * Tipovi poruka koji se šalju preko mreže
 */
public class MessageTypes
{
    public static final String PROCESS_TRANSACTION = "TRN";
    public static final String GET_PRICES = "PRQ";
    public static final String ANNOUNCE_PRICES = "PRA";
    public static final String REVOKE_TRANSACTION = "RVK";
    public static final String REVOKE_TRANSACTION_RESPONSE = "RVR";
    public static final String TRANSACTION_SUCCESS = "TRS";
    public static final String ECHO = "ECH";
    public static final String REGISTER_CLIENT = "REG";
    public static final String REGISTER_SLAVE = "RGS";
    public static final String GET_TRANSACTION_LIST = "TLG";
    public static final String GET_TRANSACTION_LIST_RESPONSE = "TLR";
}
