package arsngrobg.smphook;

import arsngrobg.smphook.annotations.UtilityClass;
import arsngrobg.smphook.server.HeapArg;

@UtilityClass
public final class SMPHook {
    public static void main(String[] args) {
        HeapArg arg1 = new HeapArg(20_000);
        HeapArg arg2 = new HeapArg(20, HeapArg.Unit.KILOBYTE);
        System.out.println(HeapArg.asMaxJVM(arg2));
        System.out.println(arg1.compareTo(arg2));
    }
}
