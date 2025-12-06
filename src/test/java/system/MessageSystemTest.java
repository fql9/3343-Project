package system;

import model.Message;
import model.UserRole;
import service.MessageService;
import service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统测试 - P2P消息通讯系统
 * 测试用户之间的完整消息交互流程
 */
@DisplayName("System Test: P2P消息通讯系统")
public class MessageSystemTest extends SystemTestBase {

    private UserService userService;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        messageService = new MessageService();
    }

    @Test
    @DisplayName("场景1: 完整的P2P消息对话")
    void testCompleteP2PConversation() {
        // ============ Phase 1: 创建用户 ============
        userService.register("alice", "pass123", "alice@test.com", UserRole.BUYER);
        userService.register("bob", "pass123", "bob@test.com", UserRole.SELLER);
        
        // 获取用户ID
        userService.login("alice", "pass123");
        Long aliceId = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("bob", "pass123");
        Long bobId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // ============ Phase 2: Alice发送消息给Bob ============
        userService.login("alice", "pass123");
        
        String sendResult1 = messageService.sendMessage(aliceId, bobId, "Hi Bob, 你的商品还在吗?");
        assertNull(sendResult1, "消息发送应该成功");
        
        String sendResult2 = messageService.sendMessage(aliceId, bobId, "我对你的iPhone很感兴趣");
        assertNull(sendResult2, "第二条消息发送应该成功");
        
        userService.logout();
        
        // ============ Phase 3: Bob接收并回复消息 ============
        userService.login("bob", "pass123");
        
        // Bob查看收到的消息
        List<Message> bobMessages = messageService.getConversation(bobId, aliceId);
        assertEquals(2, bobMessages.size(), "Bob应该收到2条消息");
        
        // Bob回复Alice
        String replyResult = messageService.sendMessage(bobId, aliceId, "在的，商品还有货");
        assertNull(replyResult, "Bob回复应该成功");
        
        userService.logout();
        
        // ============ Phase 4: Alice查看完整对话 ============
        userService.login("alice", "pass123");
        
        List<Message> conversation = messageService.getConversation(aliceId, bobId);
        assertEquals(3, conversation.size(), "对话应该有3条消息");
        
        // 验证消息顺序和内容
        assertEquals(aliceId, conversation.get(0).getFromUserId());
        assertEquals("Hi Bob, 你的商品还在吗?", conversation.get(0).getContent());
        assertEquals(bobId, conversation.get(2).getFromUserId());
        assertEquals("在的，商品还有货", conversation.get(2).getContent());
        
        userService.logout();
    }

    @Test
    @DisplayName("场景2: 多人聊天 - 一个用户与多个用户对话")
    void testMultipleConversations() {
        // 创建多个用户
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer1", "pass123", "buyer1@test.com", UserRole.BUYER);
        userService.register("buyer2", "pass123", "buyer2@test.com", UserRole.BUYER);
        
        // 获取用户ID
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("buyer1", "pass123");
        Long buyer1Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("buyer2", "pass123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        // Buyer1与卖家对话
        userService.login("buyer1", "pass123");
        messageService.sendMessage(buyer1Id, sellerId, "Buyer1: 价格能便宜点吗?");
        userService.logout();
        
        // Buyer2与卖家对话
        userService.login("buyer2", "pass123");
        messageService.sendMessage(buyer2Id, sellerId, "Buyer2: 支持当面交易吗?");
        userService.logout();
        
        // 卖家查看与不同买家的对话
        userService.login("seller", "pass123");
        
        List<Message> conv1 = messageService.getConversation(sellerId, buyer1Id);
        assertEquals(1, conv1.size(), "与Buyer1应该有1条消息");
        assertEquals("Buyer1: 价格能便宜点吗?", conv1.get(0).getContent());
        
        List<Message> conv2 = messageService.getConversation(sellerId, buyer2Id);
        assertEquals(1, conv2.size(), "与Buyer2应该有1条消息");
        assertEquals("Buyer2: 支持当面交易吗?", conv2.get(0).getContent());
        
        userService.logout();
    }

    @Test
    @DisplayName("场景3: 消息标记为已读")
    void testMarkMessagesAsRead() {
        // 创建用户
        userService.register("sender", "pass123", "sender@test.com", UserRole.BUYER);
        userService.register("receiver", "pass123", "receiver@test.com", UserRole.SELLER);
        
        userService.login("sender", "pass123");
        Long senderId = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("receiver", "pass123");
        Long receiverId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // 发送消息
        userService.login("sender", "pass123");
        messageService.sendMessage(senderId, receiverId, "Test message");
        userService.logout();
        
        // 接收者查看消息前，应该有未读消息
        userService.login("receiver", "pass123");
        
        List<Message> unreadMessages = messageService.getInbox(receiverId).stream()
            .filter(m -> !m.isRead())
            .collect(java.util.stream.Collectors.toList());
        assertEquals(1, unreadMessages.size(), "应该有1条未读消息");
        
        // 标记消息为已读
        messageService.markConversationAsRead(receiverId, senderId);
        
        List<Message> afterRead = messageService.getInbox(receiverId).stream()
            .filter(m -> !m.isRead())
            .collect(java.util.stream.Collectors.toList());
        assertEquals(0, afterRead.size(), "应该没有未读消息");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景4: 空消息不能发送")
    void testCannotSendEmptyMessage() {
        userService.register("user1", "pass123", "user1@test.com", UserRole.BUYER);
        userService.register("user2", "pass123", "user2@test.com", UserRole.SELLER);
        
        userService.login("user1", "pass123");
        Long user1Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("user2", "pass123");
        Long user2Id = UserService.getCurrentUser().getId();
        
        String result = messageService.sendMessage(user2Id, user1Id, "");
        assertEquals("Message content cannot be empty", result, "应该提示消息内容不能为空");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景5: 不能给自己发消息")
    void testCannotSendMessageToSelf() {
        userService.register("user", "pass123", "user@test.com", UserRole.BUYER);
        
        userService.login("user", "pass123");
        Long userId = UserService.getCurrentUser().getId();
        
        String result = messageService.sendMessage(userId, userId, "Test");
        assertEquals("Cannot send message to yourself", result, "应该提示不能给自己发消息");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景6: 查看未读消息数量")
    void testUnreadMessageCount() {
        // 创建用户
        userService.register("alice", "pass123", "alice@test.com", UserRole.BUYER);
        userService.register("bob", "pass123", "bob@test.com", UserRole.SELLER);
        userService.register("charlie", "pass123", "charlie@test.com", UserRole.SELLER);
        
        userService.login("alice", "pass123");
        Long aliceId = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("bob", "pass123");
        Long bobId = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("charlie", "pass123");
        Long charlieId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // Bob和Charlie都给Alice发消息
        userService.login("bob", "pass123");
        messageService.sendMessage(bobId, aliceId, "Message from Bob");
        userService.logout();
        
        userService.login("charlie", "pass123");
        messageService.sendMessage(charlieId, aliceId, "Message from Charlie 1");
        messageService.sendMessage(charlieId, aliceId, "Message from Charlie 2");
        userService.logout();
        
        // Alice检查未读消息
        userService.login("alice", "pass123");
        List<Message> unreadMessages = messageService.getInbox(aliceId).stream()
            .filter(m -> !m.isRead())
            .collect(java.util.stream.Collectors.toList());
        assertEquals(3, unreadMessages.size(), "Alice应该有3条未读消息");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景7: 删除对话")
    void testDeleteConversation() {
        userService.register("user1", "pass123", "user1@test.com", UserRole.BUYER);
        userService.register("user2", "pass123", "user2@test.com", UserRole.SELLER);
        
        userService.login("user1", "pass123");
        Long user1Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("user2", "pass123");
        Long user2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        // 发送几条消息
        userService.login("user1", "pass123");
        messageService.sendMessage(user1Id, user2Id, "Message 1");
        messageService.sendMessage(user1Id, user2Id, "Message 2");
        userService.logout();
        
        // 验证消息存在
        userService.login("user1", "pass123");
        List<Message> before = messageService.getConversation(user1Id, user2Id);
        assertEquals(2, before.size(), "应该有2条消息");
        
        // 删除对话中的所有消息
        for (Message msg : before) {
            messageService.deleteMessage(msg.getId());
        }
        
        List<Message> after = messageService.getConversation(user1Id, user2Id);
        assertEquals(0, after.size(), "删除后应该没有消息");
        
        userService.logout();
    }
}

