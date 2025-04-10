package chat.giga.langchain4j;

import chat.giga.client.auth.AuthClient;
import chat.giga.http.client.HttpResponse;
import chat.giga.model.ModelName;
import chat.giga.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GigaChatChatModelTest {

    @Mock
    chat.giga.http.client.HttpClient httpClient;
    @Mock
    AuthClient authClient;

    GigaChatChatModel model;

    ObjectMapper objectMapper = JsonUtils.objectMapper();

    @BeforeEach
    void setUp() {
        model = GigaChatChatModel.builder()
                .authClient(authClient)
                .apiHttpClient(httpClient)
                .apiUrl("hostTest")
                .defaultChatRequestParameters(GigaChatChatRequestParameters.builder().build())
                .build();
    }

    @Test
    void chat() throws JsonProcessingException {
        var body = TestData.completionChatResponse();

        when(httpClient.execute(any()))
                .thenReturn(HttpResponse.builder()
                        .body(objectMapper.writeValueAsBytes(body))
                        .build());

        var response = model.chat(
                ChatRequest.builder()
                        .messages(new UserMessage("Получить положительное значение квадратного корня из числа 25"))
                        .parameters(GigaChatChatRequestParameters.builder().modelName(ModelName.GIGA_CHAT_PRO)
                                .build())
                        .build());
        System.out.println(response);
        assertNotNull(response);
        assertThat(response.tokenUsage().inputTokenCount()).isEqualTo(body.usage().promptTokens());
        assertThat(response.aiMessage().text()).isEqualTo(body.choices().get(0).message().content());
        assertThat(response.metadata().modelName()).isEqualTo(body.model());
        assertThat(response.metadata().finishReason().name()).isEqualTo(body.choices().get(0).finishReason().name());
    }
}
