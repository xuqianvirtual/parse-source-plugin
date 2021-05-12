package top.xuqian.parse.source.generate;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import top.xuqian.parse.source.antlr.Java8Lexer;
import top.xuqian.parse.source.antlr.Java8Parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

/**
 * @author xuqianqian@sensorsdata.cn
 * @since 2021/5/12
 */
@Mojo(name = "collectClass")
public class CollectClassMojo extends AbstractMojo {

  @Parameter(property = "project.build.sourceDirectory", readonly = true, required = true)
  private File sourceDirectory;

  @Parameter(property = "project.build.testSourceDirectory", readonly = true, required = true)
  private File testSourceDirectory;

  @Parameter(property = "outputDirectory", readonly = true, required = true, defaultValue = "src${file.separator}main${file.separator}resources")
  private File outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try (OutputStreamWriter writer = createOutputStream()) {
      collectFiles(writer, sourceDirectory);
      collectFiles(writer, testSourceDirectory);
    } catch (Exception e) {
      throw new MojoExecutionException("Unable to collect class.", e);
    }
  }

  private OutputStreamWriter createOutputStream() throws IOException {
    String outputDirectoryPath = outputDirectory.getAbsolutePath();
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
    File outputFile = new File(outputDirectoryPath + File.separator
        + "collect_class.txt");
    if (outputFile.exists()) {
      Files.delete(outputFile.toPath());
    }
    if (!outputFile.createNewFile()) {
      throw new RuntimeException("create new properties file failed!");
    }
    FileOutputStream outputStream = new FileOutputStream(outputFile);
    return new OutputStreamWriter(outputStream);
  }

  private void collectFiles(OutputStreamWriter writer, File file) {
    if (file.isFile()) {
      if (file.getName().endsWith(".java")) {
        processPerFile(writer, file);
      }
    } else {
      File[] files = file.listFiles();
      if (files != null) {
        for (File sub : files) {
          collectFiles(writer, sub);
        }
      }
    }
  }

  @SneakyThrows
  private void processPerFile(OutputStreamWriter writer, File file) {
    if (!file.exists()) {
      return;
    }

    Java8Lexer lexer = new Java8Lexer(CharStreams.fromFileName(file.getPath()));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Java8Parser parser = new Java8Parser(tokens);

    // 解析 package
    Java8Parser.PackageDeclarationContext packageDeclarationContext = parser.packageDeclaration();
    Java8Parser.PackageNameContext packageNameContext = packageDeclarationContext.packageName();
    if (packageNameContext == null || packageNameContext.getText() == null) {
      return;
    }

    // 解析整个类文件
    Java8Parser.CompilationUnitContext context = parser.compilationUnit();

    List<Java8Parser.TypeDeclarationContext> typeDeclarationContexts = context.typeDeclaration();
    if (typeDeclarationContexts != null && !typeDeclarationContexts.isEmpty()) {
      for (Java8Parser.TypeDeclarationContext typeDeclarationContext : typeDeclarationContexts) {
        Java8Parser.ClassDeclarationContext classDeclarationContext = typeDeclarationContext.classDeclaration();
        // 这里只解析普通的类，没有解析枚举
        if (classDeclarationContext != null && !classDeclarationContext.isEmpty()
            && classDeclarationContext.normalClassDeclaration() != null) {
          Java8Parser.NormalClassDeclarationContext normalClassDeclarationContext =
              classDeclarationContext.normalClassDeclaration();
          String className = normalClassDeclarationContext.Identifier().getText();
          String fullClassName = packageNameContext.getText() + "." + className;
          writer.write(fullClassName + "\n");
        }
      }
    }
  }
}
