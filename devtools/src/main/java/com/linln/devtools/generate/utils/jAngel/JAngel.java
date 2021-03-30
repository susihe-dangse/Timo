package com.linln.devtools.generate.utils.jAngel;

import com.linln.devtools.generate.utils.jAngel.nodes.ClassNode;
import com.linln.devtools.generate.utils.jAngel.nodes.Document;
import com.linln.devtools.generate.utils.jAngel.parser.Expression;
import com.linln.devtools.generate.utils.jAngel.parser.Parser;
import com.linln.devtools.generate.utils.jAngel.utils.StringUtil;
import org.apache.commons.codec.Charsets;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author 小懒虫
 * @date 2019/3/28
 */
public class JAngel {

    /* 默认值区域 */
    /** 字符编码 */
    public static final String ENCODE = "UTF-8";

    /** 系统默认的换行符 */
    public static String lineBreak = System.getProperty("line.separator");

    /** 制表符(缩进距离) */
    public static String tabBreak = StringUtil.blank(4);

    /**
     * 新建一个java源码文档
     * @param clazzName 类名称
     * @return java源码文档
     */
    public static Document create(String clazzName){
        Document document = new Document();
        JAngelContainer container = new JAngelContainer();
        document.setContainer(container);
        ClassNode classNode = new ClassNode(clazzName);
        document.setClazz(classNode);
        return document;
    }

    /**
     * 解析java源码文件
     * @param path 文件路径
     * @return java源码文档
     */
    public static Document parse(String path){
        return parse(path, null);
    }

    /**
     * 解析java源码文件
     * @param path 文件路径
     * @param expression 模板表达式
     * @return java源码文档
     */
    public static Document parse(String path, Expression expression){
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String line = "";
            Parser parser = new Parser();
            try {
                fis = new FileInputStream(URLDecoder.decode(path, ENCODE));
                isr = new InputStreamReader(fis, ENCODE);
                br = new BufferedReader(isr);
                while ((line = br.readLine()) != null) {
                    if (expression != null) {
                        line = expression.matcher(line);
                    }
                    parser.line(line + lineBreak);
                }
            }catch (java.io.FileNotFoundException fne){
                if(path.matches(".*(jar|zip).*")){
                    // 从 jar 文件中读取文件
                    String multiLine = readFromZipFile(path);
                    // System.out.println(multiLine);
                    String[] lines = multiLine.split(lineBreak);
                    for (int i = 0; i < lines.length; i++) {
                        String iLine = lines[i];
                        if (expression != null) {
                            iLine = expression.matcher(iLine);
                        }
                        parser.line(iLine + lineBreak);
                    }
                }else{
                    fne.printStackTrace();
                }
            }
            return parser.getDocument();
        }catch(java.io.FileNotFoundException fne){
            // //  java.io.FileNotFoundException: file:\F:\project\Timo\Timo\admin\target\Timo-2.0.3.jar!\BOOT-INF\lib\devtools-2.0.3.jar!\com\linln\devtools\generate\template\RepositoryTemplate.tpl (文件名、目录名或卷标语法不正确。)
            // System.out.println("FileNotFoundException fne");
            // System.out.println(path);
            // if(path.indexOf("file:/") == 0 && path.indexOf("file://") != 0){
            //     path = path.replace("file:/", "file:///");
            // }
            // // path = path.replace("file:///", "file:/");
            // // path = path.replace("file:/", "");
            // System.out.println(path);
            // try {
            //     path = URLDecoder.decode(path, ENCODE);
            // } catch (UnsupportedEncodingException e) {
            //     e.printStackTrace();
            // }
            // System.out.println(path);
            // try {
            //     // fis = new FileInputStream(URLDecoder.decode(path, ENCODE));
            //     fis = new FileInputStream(path);
            //     System.out.println(fis.getFD().toString());
            // } catch (FileNotFoundException e) {
            //     e.printStackTrace();
            // } catch (UnsupportedEncodingException e) {
            //     e.printStackTrace();
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
            fne.printStackTrace();
        } catch ( IOException e) {
            System.out.println("IOException e");
            try {
                if(fis != null) {
                    fis.close();
                }
                if(isr != null) {
                    isr.close();
                }
                if(br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Document();
    }

    public static String readFromZipFile(String path) {
        // path = "F:/project/Timo/Timo/admin/target/Timo-2.0.3.jar!/BOOT-INF/lib/devtools-2.0.3.jar!/com/linln/devtools/generate/template/RepositoryTemplate.tpl";
        path = path.replaceFirst("^file:/", "");
        // System.out.println(path);
        String result = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        InputStream zis = null;
        try {
            String paths[] = path.split("!/");
            for (int i = 0; i < paths.length; i++) {
                String item = paths[i];
                String nextItem = "";
                if(i < paths.length - 1 ){
                    nextItem = paths[i+1];
                }else{
                    nextItem = "";
                }
                // System.out.println("item : "+item);
                // System.out.println("    nextItem : "+nextItem);
                if(null == fis ) {
                    fis = new FileInputStream(item);
                }else{}
                if(null == zis){
                    zis =  readZipStream(fis, nextItem);
                }else if(zis.available() == 1){
                    zis =  readZipStream(zis, nextItem);
                }else{
                    zis =  readZipStream(zis, nextItem);
                }
                if(i == paths.length - 2 ){
                    // read content to string
                    //Verify original content
                    result = readContentsToStr(zis);
                    // System.out.println("        DONE readContentsToStr");
                    // System.out.println(result);
                    return result;
                }else{
                    // System.out.println( i +"  != " +(paths.length - 2) );
                }
            }
            // result = readContentsToStr(zis);
            // System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if(fis != null) {
                    fis.close();
                }
                if(isr != null) {
                    isr.close();
                }
                if(br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            // System.out.println(path);
        }
        return result;
    }

    private String decodeURL(String path) throws UnsupportedEncodingException {
        path = URLDecoder.decode(path, ENCODE);
        return path;
    }

    public static InputStream readZipStream(InputStream in, String pathStr) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry;
        FilterInputStream fis = null;
        // System.out.println("  pathStr: " + pathStr);
        int count =0;
        // System.out.println("zipIn.available(): " + zipIn.available());
        while ((entry = zipIn.getNextEntry()) != null) {
            String entryName = entry.getName();
            // if(count++ == 2){
            //     System.out.println(entryName);
            // }
            // if(entryName.matches(".*(tpl)")) {
            //     System.out.println("     FOUND " + entryName);
            // }
            if( null == pathStr || pathStr.equals("") ){
                // no child node to be found, so it is the child itself, return content.
                // System.out.println("pathStr is empty");
                fis = new FilterInputStream(zipIn) {
                    @Override
                    public void close() throws IOException {
                        zipIn.closeEntry();
                    }
                };
                // readContents(fis);
                // System.out.println(entryName);
            }else if(entryName.equals(pathStr)){
                // System.out.println("    pathStr equals entryName : " + entryName);
                if(entryName.matches(".*(jar|zip)")) {
                    // read zip file
                    return readContents(fis);
                }else{
                    // return content
                    fis = new FilterInputStream(zipIn) {
                        @Override
                        public void close() throws IOException {
                            zipIn.closeEntry();
                        }
                    };
                    // System.out.println(entryName);
                    return readContents(fis);
                }
            }else if(entryName.indexOf(pathStr) != -1){
                // System.out.println("   entryName contains pathStr: " + entryName);
                fis = new FilterInputStream(zipIn) {
                    @Override
                    public void close() throws IOException {
                        zipIn.closeEntry();
                    }
                };
                // System.out.println(entryName);
            }else{
                // System.out.println("  " + pathStr);
                // if(entryName.matches(".*(tpl)")) {
                //     System.out.println("     ELSE " + entryName);
                // }
                zipIn.closeEntry();
            }
            // System.out.println(entry.getName());
            // readContentsInfo(new FilterInputStream(zipIn) {
            //     @Override
            //     public void close() throws IOException {
            //         zipIn.closeEntry();
            //     }
            // });
        }
        // for (int i = count; i > 0; i--) {
        //     System.out.print("+");
        // }
        // System.out.println("+");
        return zipIn;
    }

    private static InputStream readContents(InputStream contentsIn) throws IOException {
        // byte contents[] = new byte[4096];
        // int direct;
        // while ((direct = contentsIn.read(contents, 0, contents.length)) >= 0) {
        //     System.out.println(Arrays.toString(contents));
        // }
        return contentsIn;
    }

    private static String readContentsToDocument(InputStream contentsIn) throws IOException {
        byte contents[] = new byte[4096];
        int direct;
        String result = "";
        while ((direct = contentsIn.read(contents, 0, contents.length)) >= 0) {
            // System.out.println(Arrays.toString(contents));
            //Base64 Encoded
            String encoded = Base64.getEncoder().encodeToString(contents);

            //Base64 Decoded
            byte[] decoded = Base64.getDecoder().decode(encoded);
            result += new String(decoded);
        }
        return result;
    }

    private static String readContentsToStr(InputStream contentsIn) throws IOException {
        byte contents[] = new byte[4096];
        int direct;
        String result = "";
        while ((direct = contentsIn.read(contents, 0, contents.length)) >= 0) {
            // System.out.println(Arrays.toString(contents));
            System.out.println("Read " + direct + "bytes content.");
            //Base64 Encoded
            String encoded = Base64.getEncoder().encodeToString(
                    Arrays.copyOfRange(contents,0, direct)
            );
            //Base64 Decoded
            byte[] decoded = Base64.getDecoder().decode(encoded);
            result += (new String(decoded, Charsets.UTF_8) );
            contents = new byte[4096];
        }
        return result;
    }

    private static void readContentsInfo(InputStream contentsIn) throws IOException {
        byte contents[] = new byte[4096];
        int direct;
        while ((direct = contentsIn.read(contents, 0, contents.length)) >= 0) {
            System.out.println("Read " + direct + "bytes content.");
        }
    }

    public static void main(String[] args) throws IOException {
        //  java.io.FileNotFoundException: file:\F:\project\Timo\Timo\admin\target\Timo-2.0.3.jar!\BOOT-INF\lib\devtools-2.0.3.jar!\com\linln\devtools\generate\template\RepositoryTemplate.tpl (文件名、目录名或卷标语法不正确。)
        String path="";
        path = "file:/F:/project/Timo/Timo/admin/target/Timo-2.0.3.jar!/BOOT-INF/lib/devtools-2.0.3.jar!/com/linln/devtools/generate/template/RepositoryTemplate.tpl";
        System.out.println(path.replaceFirst("^file:/",""));
        FileInputStream fis = null;
        path = "F:/project/Timo/Timo/admin/target/Timo-2.0.3.jar";
        fis = new FileInputStream(path);
        System.out.println(path);
        path = "F:/project/Timo/Timo/admin/target/Timo-2.0.3.jar";
        fis = new FileInputStream(path);
        System.out.println(path);

        try{
            readZipStream(fis, "BOOT-INF/lib/devtools-2.0.3.jar");
        }catch (Exception ex){
            System.out.println(path);
        }

        // path = "F:/project/Timo/Timo/admin/target/Timo-2.0.3.jar!/BOOT-INF/lib/devtools-2.0.3.jar";
        // fis = new FileInputStream(path);
        // System.out.println(path);
        // path = "F:/project/Timo/Timo/admin/target/Timo-2.0.3.jar!/BOOT-INF/lib/devtools-2.0.3.jar!/com/linln/devtools/generate/template/RepositoryTemplate.tpl";
        // fis = new FileInputStream(path);
        // System.out.println(path);
        // path = "file:/F:/project/Timo/Timo/admin/target/Timo-2.0.3.jar";
        // fis = new FileInputStream(path);
    }
}
