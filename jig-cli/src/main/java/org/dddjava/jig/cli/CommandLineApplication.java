package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzeStatuses;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.raw.RawSourceLocations;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.HandlerMethodArgumentResolver;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@SpringBootApplication
public class CommandLineApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CommandLineApplication.class, args);
    }

    @Autowired
    CliConfig cliConfig;

    @Override
    public void run(String... args) {
        ResourceBundle jigMessages = ResourceBundle.getBundle("jig-messages");
        List<JigDocument> jigDocuments = cliConfig.jigDocuments();
        Configuration configuration = cliConfig.configuration();

        LOGGER.info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", cliConfig.propertiesText());

        long startTime = System.currentTimeMillis();
        ImplementationService implementationService = configuration.implementationService();
        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

        RawSourceLocations rawSourceLocations = cliConfig.rawSourceLocations();
        AnalyzedImplementation implementations = implementationService.implementations(rawSourceLocations);

        AnalyzeStatuses status = implementations.status();
        if (status.hasError()) {
            LOGGER.warn(jigMessages.getString("failure"), status.errorLogText());
            return;
        }
        if (status.hasWarning()) {
            LOGGER.warn(jigMessages.getString("implementation.warnings"), status.warningLogText());
        }

        List<HandleResult> handleResultList = new ArrayList<>();
        Path outputDirectory = cliConfig.outputDirectory();
        for (JigDocument jigDocument : jigDocuments) {
            HandleResult result = jigDocumentHandlers.handle(jigDocument, new HandlerMethodArgumentResolver(implementations), outputDirectory);
            handleResultList.add(result);
        }

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePaths())
                .collect(Collectors.joining("\n"));
        LOGGER.info("-- output documents -------------------------------------------\n{}\n------------------------------------------------------------", resultLog);
        LOGGER.info(jigMessages.getString("success"), System.currentTimeMillis() - startTime);
    }
}
