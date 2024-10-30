package arsngrobg.smphook;

import arsngrobg.smphook.server.HeapArg;
import arsngrobg.smphook.server.HeapUnit;

public final class SMPHook {
    private SMPHook() { throw new UnsupportedOperationException("SMPHook is a utility class."); }

    public static void main(String[] args) {
        HeapArg arg1 = new HeapArg(2, HeapUnit.GIGABYTE);
        HeapArg arg2 = new HeapArg(2, HeapUnit.GIGABYTE);
        System.out.println(arg1.compareTo(arg2));
        System.out.println(arg1);
    }
}
