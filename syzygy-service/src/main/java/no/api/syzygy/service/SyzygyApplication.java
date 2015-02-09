package no.api.syzygy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyzygyApplication {
    private static final Logger log = LoggerFactory.getLogger(SyzygyApplication.class);
    public static void main(String[] args) {
        SyzygyApplication app = new SyzygyApplication();
        switch (args.length) {
            case 1:
                new SyzygyApplication().doMojo();
                break;
            case 2:
                break;
            default:
                app.instructions();
        }


    }

    private void instructions() {
        System.out.println("You need to supply 2 arguments: ");
        System.out.println("  path/to/file.yaml");
        System.out.println("  target URL for etcd");
        System.out.println("  etcd mount point, usually filename. It becomes /syzygy/config/");

        System.out.println("Example:");
        System.out.println("   path/to/config.yaml ");
        System.out.println("   http://127.0.0.1:4001/v2/ ");
        System.out.println("   config");
    }

    private void doMojo() {

    }
}