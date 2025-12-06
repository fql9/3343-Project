# CS3343 Group 11 - Design Constraints & Design Patterns

本文档包含 Second-Hand Trading Platform 项目的设计约束和设计模式详细说明。

---

## 1. Design Constraints (设计约束)

### 1.1 Technical Constraints (技术约束)

| Constraint | Description |
|------------|-------------|
| **Programming Language** | Java 21 或更高版本，使用现代 Java 特性如 Text Blocks、Record 等 |
| **UI Framework** | JavaFX 23.0.1，用于构建跨平台桌面 GUI 应用 |
| **Database** | SQLite 3.46.0.0，轻量级嵌入式数据库，无需额外服务器配置 |
| **Build Tool** | Gradle 9.2.1，用于依赖管理和构建自动化 |
| **Testing Framework** | JUnit 5.10.2 + Mockito 5.14.2，支持单元测试和集成测试 |

### 1.2 Security Constraints (安全约束)

| Constraint | Description |
|------------|-------------|
| **Password Encryption** | 所有用户密码使用 SHA-256 算法进行单向哈希加密，原始密码不存储在数据库中 |
| **Database URL Validation** | 系统强制验证数据库 URL 必须以 `jdbc:sqlite:` 开头，防止恶意数据库连接注入 |
| **Role-Based Access Control** | 实现三级角色权限系统：BUYER（买家）、SELLER（卖家）、ADMIN（管理员），不同角色有不同的功能访问权限 |
| **Input Validation** | 所有用户输入通过 `ValidationUtils` 进行严格验证，防止非法数据进入系统 |

### 1.3 Data Validation Constraints (数据验证约束)

| Field | Validation Rule |
|-------|-----------------|
| **Username** | 3-20 个字符，只允许字母、数字和下划线 (`^[A-Za-z0-9_]{3,20}$`) |
| **Password** | 最少 6 个字符 |
| **Email** | 可选字段，但如果填写必须符合标准邮箱格式 |
| **Price** | 必须大于 0 |
| **Item Title** | 不能为空 |
| **Shipping Address** | 订单创建时不能为空 |

### 1.4 Order State Machine Constraints (订单状态机约束)

系统实现了严格的订单状态转换控制：

```
         ┌────────────────────────────────────┐
         │                                    │
    ┌────┴────┐      ┌─────────┐      ┌───────┴───────┐
    │  PAID   │ ───► │ SHIPPED │ ───► │  COMPLETED    │
    └────┬────┘      └────┬────┘      └───────────────┘
         │                │
         │                │
         ▼                ▼
    ┌─────────────────────────┐
    │      CANCELLED          │
    └─────────────────────────┘
```

| Transition | Allowed By | Business Rule |
|------------|------------|---------------|
| PAID → SHIPPED | Seller Only | 卖家确认发货后更新状态 |
| SHIPPED → COMPLETED | Buyer Only | 买家确认收货后完成订单 |
| Any → CANCELLED | Both Parties | 已完成订单不可取消 |

### 1.5 Platform Constraints (平台约束)

| Constraint | Description |
|------------|-------------|
| **Cross-Platform Support** | 支持 Windows、macOS、Linux 操作系统 |
| **Image Storage** | 商品图片存储在本地 `item_images/` 文件夹，支持 JPG、PNG、GIF、BMP 格式 |
| **Database File** | 数据库文件 `secondhand.db` 存储在项目根目录，应用启动时自动创建表结构 |
| **Concurrent Access** | 使用 SQLite WAL 模式和 10 秒超时处理并发访问 |

### 1.6 Data Integrity Constraints (数据完整性约束)

| Constraint Type | Description |
|-----------------|-------------|
| **Primary Key** | 所有表使用自增 `id` 作为主键 |
| **Foreign Key** | 商品关联卖家、订单关联买家/卖家/商品、消息关联发送者/接收者 |
| **Unique Constraint** | 用户名唯一、邮箱唯一、收藏记录（用户+商品）组合唯一 |
| **Not Null** | 关键字段如用户名、密码哈希、商品标题、价格等不允许为空 |

### 1.7 Business Rule Constraints (业务规则约束)

| Rule | Description |
|------|-------------|
| **Self-Purchase Prevention** | 用户不能购买自己发布的商品 |
| **Item Availability** | 商品被购买后自动标记为不可用（active = false） |
| **Account Status** | 被管理员禁用的账户无法登录 |
| **Review Requirement** | 用户只能在订单完成后对卖家进行评价 |

---

## 2. Design Patterns (设计模式)

### 2.1 Layered Architecture Pattern (分层架构模式)

项目采用经典的**四层架构**设计，实现关注点分离：

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (LoginController, MainController, BoardController...)   │
├─────────────────────────────────────────────────────────┤
│                     Service Layer                        │
│  (UserService, ItemService, OrderService, ...)           │
├─────────────────────────────────────────────────────────┤
│                  Data Access Layer (DAO)                 │
│  (UserDaoImpl, ItemDaoImpl, OrderDaoImpl, ...)           │
├─────────────────────────────────────────────────────────┤
│                      Model Layer                         │
│  (User, Item, Order, Message, Favorite, Review, ...)     │
└─────────────────────────────────────────────────────────┘
```

**Benefits (优点):**
- **可维护性**: 每层职责明确，修改一层不影响其他层
- **可测试性**: 可以独立测试每一层
- **可复用性**: 服务层和 DAO 层可以被多个控制器复用

### 2.2 Data Access Object (DAO) Pattern

项目使用 **DAO 模式** 将数据访问逻辑与业务逻辑分离。

#### 类图结构:

```
        <<interface>>                    
           UserDao                       
    ┌─────────────────────┐              
    │ + findByUsername()  │              
    │ + findById()        │              
    │ + findAll()         │◄────────────┐
    │ + save()            │             │ implements
    │ + update()          │             │
    │ + delete()          │             │
    └─────────────────────┘             │
              ▲                         │
              │                         │
    ┌─────────┴───────────┐             │
    │    UserDaoImpl      │─────────────┘
    │                     │
    │ - mapRow(ResultSet) │
    └─────────────────────┘
```

#### 实现示例:

```java
// Interface definition (dao/UserDao.java)
public interface UserDao {
    User findByUsername(String username);
    User findById(Long id);
    List<User> findAll();
    void save(User user);
    void update(User user);
    void delete(Long id);
}

// Implementation (dao/impl/UserDaoImpl.java)
public class UserDaoImpl implements UserDao {
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        // ... more field mappings
        return u;
    }
}
```

**Benefits (优点):**
- **解耦**: 业务逻辑不依赖具体的数据库实现
- **可测试性**: 可以使用 Mock 对象替换真实 DAO 进行单元测试
- **可维护性**: 更换数据库只需修改 DAO 实现类

### 2.3 Model-View-Controller (MVC) Pattern

项目采用 **MVC 模式** 分离用户界面和业务逻辑。

```
┌─────────────┐         ┌─────────────────────┐
│    User     │ ───────►│     Controller      │
│  (Action)   │         │ (LoginController,   │
└─────────────┘         │  MainController)    │
      ▲                 └──────────┬──────────┘
      │                            │
      │                            │ Updates
      │                            ▼
      │                 ┌──────────────────────┐
      │   Displays      │       Model          │
      │◄────────────────│  (User, Item, Order) │
      │                 └──────────────────────┘
      │                            │
      │                            │ Notifies
┌─────┴───────┐                    ▼
│    View     │◄───────────────────┘
│  (JavaFX    │
│   Scenes)   │
└─────────────┘
```

#### 控制器示例:

```java
public class LoginController {
    private Stage primaryStage;        // View reference
    private UserService userService;   // Model/Service reference
    
    public void showLoginView() {
        // Build and display View
        VBox root = new VBox(20);
        // ... UI setup
        primaryStage.setScene(scene);
    }
    
    private void handleLogin() {
        String error = userService.login(username, password);
        if (error != null) {
            DialogUtils.showError("Login Failed", error);
        } else {
            // Navigate to main view
            new MainController(primaryStage).showMainView();
        }
    }
}
```

### 2.4 Service Layer Pattern (服务层模式)

项目使用 **Service Layer** 封装复杂的业务逻辑，作为控制器和 DAO 之间的中介。

```
Controller ──► Service ──► DAO ──► Database
                  │
                  ▼
           Validation
           Business Rules
           Transaction Coordination
```

#### 服务层职责示例:

```java
public class OrderService {
    private final OrderDao orderDao;
    private final ItemDao itemDao;
    private final NotificationService notificationService;

    public String createOrder(Long buyerId, Long itemId, String shippingAddress) {
        // 1. Validate item exists and is active
        Item item = itemDao.findById(itemId);
        if (item == null) return "Item not found";
        if (!item.isActive()) return "Item is no longer available";
        
        // 2. Business rule: prevent self-purchase
        if (item.getSellerId().equals(buyerId)) {
            return "Cannot buy your own item";
        }
        
        // 3. Create and save order
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString());
        // ... set other fields
        orderDao.save(order);
        
        // 4. Update item availability
        item.setActive(false);
        itemDao.update(item);
        
        // 5. Send notification to seller
        notificationService.createNotification(
            item.getSellerId(), 
            "New Order Received", 
            "You have a new order for: " + item.getTitle()
        );
        
        return null; // Success
    }
}
```

### 2.5 Dependency Injection Pattern (依赖注入模式)

项目使用**构造函数注入**实现依赖注入，提高可测试性。

#### 实现示例:

```java
public class ItemService {
    private final ItemDao itemDao;
    
    // Default constructor for production use
    public ItemService() {
        this(new ItemDaoImpl());
    }
    
    // Constructor for dependency injection (testing)
    public ItemService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }
    
    public Item getItemById(Long itemId) {
        return itemDao.findById(itemId);
    }
}
```

#### 单元测试中使用 Mock:

```java
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemDao mockItemDao;
    
    private ItemService itemService;
    
    @BeforeEach
    void setUp() {
        // Inject mock DAO
        itemService = new ItemService(mockItemDao);
    }
    
    @Test
    void testGetItemById() {
        Item mockItem = new Item();
        mockItem.setId(1L);
        when(mockItemDao.findById(1L)).thenReturn(mockItem);
        
        Item result = itemService.getItemById(1L);
        
        assertEquals(1L, result.getId());
        verify(mockItemDao).findById(1L);
    }
}
```

### 2.6 Singleton Pattern (变体) - Session Management

项目使用**静态字段**管理当前登录用户的会话状态。

```java
public class UserService {
    // Static field for current logged-in user (Singleton-like session)
    private static User currentUser;
    
    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        // ... validation logic
        currentUser = user;  // Store session
        return null;
    }
    
    public void logout() {
        currentUser = null;  // Clear session
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }
}
```

### 2.7 Utility/Helper Pattern (工具类模式)

项目使用**工具类**集中管理通用功能，所有方法为静态方法。

```
┌──────────────────────────────────────────────────────────┐
│                    Utility Classes                        │
├──────────────────┬───────────────────┬───────────────────┤
│  ValidationUtils │   PasswordUtils   │   DialogUtils     │
├──────────────────┼───────────────────┼───────────────────┤
│ isValidUsername()│ hashPassword()    │ showInfo()        │
│ isValidPassword()│ verifyPassword()  │ showWarning()     │
│ isValidEmail()   │ generateRandom()  │ showError()       │
│ isValidPrice()   │                   │ showConfirm()     │
│ isNotEmpty()     │                   │ showSuccess()     │
└──────────────────┴───────────────────┴───────────────────┘
```

#### 密码工具类示例:

```java
public class PasswordUtils {
    private static final String ALGORITHM = "SHA-256";
    
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password encryption failed", e);
        }
    }
    
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equals(hashedPassword);
    }
}
```

### 2.8 Observer Pattern (观察者模式 - 应用于通知系统)

项目的通知系统体现了**观察者模式**的思想：当订单状态变化时，自动通知相关用户。

```
┌─────────────────┐        ┌───────────────────────┐
│   OrderService  │ ──────►│  NotificationService  │
│  (Publisher)    │ create │    (Notifier)         │
└─────────────────┘        └───────────┬───────────┘
                                       │
                                       │ create
                                       ▼
                           ┌───────────────────────┐
                           │    Notification       │
                           │  (Stored in DB)       │
                           └───────────────────────┘
                                       │
                                       │ poll
                                       ▼
                           ┌───────────────────────┐
                           │   MainController      │
                           │   (Subscriber/UI)     │
                           └───────────────────────┘
```

#### 实现示例:

```java
// In OrderService - publishing notifications
public String updateOrderStatus(Long orderId, String newStatus, Long operatorId) {
    Order order = orderDao.findById(orderId);
    // ... validation and update logic
    
    order.setStatus(newStatus);
    orderDao.update(order);
    
    // Notify the other party (Observer pattern)
    Long targetUserId = isSeller ? order.getBuyerId() : order.getSellerId();
    notificationService.createNotification(
        targetUserId, 
        "Order Status Updated",
        "Order " + order.getOrderNo() + " status changed to " + newStatus
    );
    return null;
}

// In MainController - polling for notifications (subscriber)
private void startNotificationPoller() {
    notificationPoller = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
        if (UserService.getCurrentUser() != null) {
            checkNewNotifications();
            updateUnreadCount();
        }
    }));
    notificationPoller.setCycleCount(Timeline.INDEFINITE);
    notificationPoller.play();
}
```

---

## 3. Design Patterns Summary Table (设计模式总结表)

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Layered Architecture** | 整体项目结构 | 分离关注点，提高可维护性 |
| **DAO Pattern** | `dao/` 和 `dao/impl/` | 封装数据访问，解耦业务与数据库 |
| **MVC Pattern** | `controller/`, `model/`, JavaFX Views | 分离 UI、业务逻辑和数据 |
| **Service Layer** | `service/` | 封装业务规则，协调多个 DAO |
| **Dependency Injection** | Service 构造函数 | 提高可测试性 |
| **Singleton (Session)** | `UserService.currentUser` | 管理用户会话状态 |
| **Utility/Helper** | `util/` | 集中管理通用功能 |
| **Observer (Notification)** | `OrderService` + `NotificationService` | 事件驱动的通知机制 |

---

## 4. Class Diagram Overview (类图概览)

```
                              ┌────────────────────┐
                              │      MainApp       │
                              │  (Application)     │
                              └─────────┬──────────┘
                                        │
                                        ▼
┌─────────────────┐          ┌────────────────────┐
│  DatabaseConfig │◄─────────│  LoginController   │
│   (Singleton)   │          └─────────┬──────────┘
└─────────────────┘                    │
         ▲                             ▼
         │                   ┌────────────────────┐
         │                   │   MainController   │──┬──► BoardController
         │                   └─────────┬──────────┘  │
         │                             │             ├──► MyItemsController
         │                             ▼             │
         │                   ┌────────────────────┐  ├──► OrderHistoryController
         │                   │   UserService      │  │
         │                   │   ItemService      │  ├──► FavoritesController
         │                   │   OrderService     │  │
         │                   │   MessageService   │  └──► MessageController
         │                   │   ...              │
         │                   └─────────┬──────────┘
         │                             │
         │                             ▼
         │                   ┌────────────────────┐
         └───────────────────│     DAO Layer      │
                             │  UserDaoImpl       │
                             │  ItemDaoImpl       │
                             │  OrderDaoImpl      │
                             │  ...               │
                             └─────────┬──────────┘
                                       │
                                       ▼
                             ┌────────────────────┐
                             │   SQLite Database  │
                             │  (secondhand.db)   │
                             └────────────────────┘
```

---

*This document was generated based on the analysis of the Second-Hand Trading Platform source code.*

