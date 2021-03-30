package com.linln.devtools.generate.template;

import com.linln.devtools.generate.domain.Generate;
import com.linln.devtools.generate.enums.TierType;
import com.linln.devtools.generate.utils.FileUtil;
import com.linln.devtools.generate.utils.GenerateUtil;
import com.linln.devtools.generate.utils.jAngel.JAngelContainer;
import com.linln.devtools.generate.utils.jAngel.nodes.Document;
import com.linln.devtools.generate.utils.jAngel.parser.Expression;
import com.linln.devtools.generate.utils.parser.JavaParseUtil;
import com.linln.modules.system.repository.BaseRepository;

import java.nio.file.FileAlreadyExistsException;
import java.util.Set;

/**
 * @author 小懒虫
 * @date 2018/10/25
 */
public class RepositoryTemplate {

    /**
     * 生成需要导入的包
     */
    private static Set<String> genImports(Generate generate) {
        JAngelContainer container = new JAngelContainer();
        container.importClass(JavaParseUtil.getPackage(generate, TierType.DOMAIN));
        container.in(generate.getBasic().getPackagePath()).importClass(BaseRepository.class);
        return container.getImports();
    }

    /**
     * 生成类字段
     */
    private static Document genClazzBody(Generate generate) {
        // 构建数据-模板表达式
        Expression expression = new Expression();
        expression.label("entity", generate.getBasic().getTableEntity());
        String path = FileUtil.templatePath(RepositoryTemplate.class);
        // TODO  java.io.FileNotFoundException: file:\F:\project\Timo\Timo\admin\target\Timo-2.0.3.jar!\BOOT-INF\lib\devtools-2.0.3.jar!\com\linln\devtools\generate\template\RepositoryTemplate.tpl (文件名、目录名或卷标语法不正确。)
        String splitStr = path.substring(5,6);
        // System.out.println(splitStr);
        // path = path.replaceAll("admin"+splitStr+"target"+splitStr+".*"+splitStr+"com",
        //         "devtools"
        //                 + splitStr + "src"
        //                 + splitStr + "main"
        //                 + splitStr + "java"
        //                 + splitStr + "com");
        // path = path.replace("file:///", "file:/");
        // path = path.replace("file:/", "");
        // path = path.replace("file:"+splitStr, "");
        // path = "jar:" + path;
        // System.out.println(path);

        // 获取jAngel文档对象
        Document document = JavaParseUtil.document(path, expression, generate, TierType.DAO);
        document.getContainer().importClass(genImports(generate));

        return document;
    }

    /**
     * 生成Dao层模板
     */
    public static String generate(Generate generate) {
        // 生成文件
        String filePath = GenerateUtil.getJavaFilePath(generate, TierType.DAO);
        try {
            Document document = genClazzBody(generate);
            GenerateUtil.generateFile(filePath, document.content());
        } catch (FileAlreadyExistsException e) {
            return GenerateUtil.fileExist(filePath);
        }
        return filePath;
    }
}
