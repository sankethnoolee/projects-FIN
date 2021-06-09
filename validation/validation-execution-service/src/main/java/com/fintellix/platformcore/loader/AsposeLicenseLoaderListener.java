package com.fintellix.platformcore.loader;

import com.aspose.cells.CellsHelper;
import com.aspose.cells.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.util.Base64;

@WebListener
public class AsposeLicenseLoaderListener implements ServletContextListener {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final String key = "!cr3@t3#b@nk!ng!nt3ll!53n53#1601";
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    ResourceLoader resourceLoader;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.info("Setting up fonts...");
        try {
            CellsHelper.setFontDir(resourceLoader.getResource("classpath:fonts" + File.separator).getFile().getPath());
        } catch (Exception e) {
            LOGGER.error("Error encountered trying to set custom fonts!", e);
        }

        LOGGER.info("Loading Aspose license - Start");

        try {
            File licenseFile = resourceLoader.getResource("classpath:licenses" + File.separator + "Aspose.Cells.Java.lic").getFile();
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            InputStream inputStream = new FileInputStream(licenseFile);
            byte[] inputBytes = new byte[(int) licenseFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(Base64.getDecoder().decode(inputBytes));
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputBytes);

            License license = new License();
            license.setLicense(byteArrayInputStream);

            byteArrayInputStream.close();
            inputStream.close();

            LOGGER.info("Aspose.Cells license set = " + License.isLicenseSet());
        } catch (Exception e) {
            LOGGER.error("Error encountered trying to set Aspose.Cells license!", e);
        }
    }
}