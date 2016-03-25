package com.lcy.plugin;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by loucyin on 2016/3/25.
 */
public class TranslationAction extends AnAction {

    //"http://fanyi.youdao.com/openapi.do?keyfrom=studyApi2016&key=912558092&type=data&doctype=json&callback=show&version=1.1&q=";

    private static final String HOST = "fanyi.youdao.com";
    private static final String PATH = "/openapi.do";

    private static final String PARAM_KEY_FROM = "keyfrom";
    private static final String KEY_FROM = "studyApi2016";

    private static final String PARAM_KEY = "key";
    private static final String KEY = "912558092";

    private static final String PARAM_TYPE = "type";
    private static final String TYPE = "data";

    private static final String PARAM_DOC_TYPE = "doctype";
    private static final String DOC_TYPE = "json";

    private static final String PARAM_CALL_BACK = "callback";
    private static final String CALL_BACK = "show";

    private static final String PARAM_VERSION = "version";
    private static final String VERSION = "1.1";

    private static final String PARAM_QUERY = "q";

    @Override
    public void actionPerformed(AnActionEvent e) {

        //获取编辑器
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            SelectionModel model = editor.getSelectionModel();

            //获取选中的文本
            String selectedText = model.getSelectedText();

            if (selectedText != null) {
                //翻译并显示
                getTranslation(selectedText);
            }
        }
    }

    private void getTranslation(String query) {
        try {
            //获取URI
            URI uri = createTranslationURI(query);
            System.out.println(uri.toString());
            //配置GET请求
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000).build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            HttpClient client = HttpClients.createDefault();

            //请求网络
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                //获取响应数据
                HttpEntity resEntity = response.getEntity();
                String json = EntityUtils.toString(resEntity, "UTF-8");

                //转化为Translation对象
                Gson gson = new Gson();
                Translation translation = gson.fromJson(json, Translation.class);

                //显示结果
                Messages.showMessageDialog(
                        translation.toString(),
                        "翻译结果",
                        Messages.getInformationIcon()
                );

            } else {
                //显示错误代码和错误信息
                Messages.showMessageDialog(
                        response.getStatusLine().getReasonPhrase(),
                        "错误代码：" + status,
                        Messages.getInformationIcon()
                );
            }
        } catch (IOException e) {
            //显示异常信息
            Messages.showMessageDialog(
                    e.getMessage(),
                    "啊欧，崩溃了",
                    Messages.getInformationIcon()
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成URI
     *
     * @param query 查询内容
     * @return URI
     * @throws URISyntaxException
     */
    private URI createTranslationURI(String query) throws URISyntaxException {

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost(HOST)
                .setPath(PATH)
                .addParameter(PARAM_KEY_FROM, KEY_FROM)
                .addParameter(PARAM_KEY, KEY)
                .addParameter(PARAM_TYPE, TYPE)
                .addParameter(PARAM_VERSION, VERSION)
                .addParameter(PARAM_DOC_TYPE, DOC_TYPE)
                .addParameter(PARAM_CALL_BACK, CALL_BACK)
                .addParameter(PARAM_QUERY, query)
        ;
        return builder.build();
    }
}
