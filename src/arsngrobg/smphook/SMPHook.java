package arsngrobg.smphook;

@UtilityClass
public final class SMPHook {
    public static void main(String[] args) {
        //1311443813700472933
        //QRUy-kzfo9KDGSDluueugBxjILRWX2Kr1OG-oubnyOpRk_AOzksD756hM3wK7POuGWye
        DiscordWebhook webhook = new DiscordWebhook("1311443813700472933", "QRUy-kzfo9KDGSDluueugBxjILRWX2Kr1OG-oubnyOpRk_AOzksD756hM3wK7POuGWye");
        webhook.post("{\"content\": \"Hi\"}");
    }
}
