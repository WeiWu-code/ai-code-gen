package xd.ww.wwaicodegen.langgraph4j.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import xd.ww.wwaicodegen.ai.model.StreamMessage;
import xd.ww.wwaicodegen.ai.model.StreamMessageTypeEnum;

/**
 * AI 响应消息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class NodeResponseMessage extends StreamMessage {

    // 节点名字
    private String name;

    // 节点状态，正在执行 Or 完毕
    private String state;

    public NodeResponseMessage(String name, String state) {
        super(StreamMessageTypeEnum.NODE_RESPONSE.getValue());
        this.name = name;
        this.state = state;
    }
}