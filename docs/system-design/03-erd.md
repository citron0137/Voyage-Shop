# ERD
### 공통 고려사항
* enum들에 대해 추후 확장성(마이그레이션의 편의)을 위해 string으로 선언

### 사용자 포인트 도메인
```mermaid
erDiagram
    T_USER_POINT {
        string user_point_id
        string user_id
        number point
        DateTime created_at
        DateTime updated_at
    }
    T_USER_POINT ||..|| T_USER: user_id 
```

#### 설계 이유
* **별도 테이블로 분리**: 사용자 정보와 포인트 정보를 분리하여 포인트 관련 트랜잭션이 사용자 테이블에 영향을 주지 않도록 설계했습니다.
* **단순한 구조**: 포인트는 단순히 증감만 있기 때문에 복잡한 관계나 필드 없이 단순하게 구성했습니다.

### 상품 도메인
```mermaid
erDiagram
    T_PRODUCT {
        string product_id
        number price
        number stock 
        DateTime created_at
        DateTime updated_at
    }
```

#### 설계 이유
* **최소한의 필드**: 기본적인 상품 정보만 포함하여 확장성을 고려했습니다. 추후 카테고리, 상세 정보 등은 별도 테이블로 확장할 수 있습니다.
* **재고 관리**: stock 필드를 통해 실시간 재고 관리가 가능하도록 했습니다.

### 쿠폰_사용자 도메인
```mermaid
erDiagram
    T_COUPON_USER {
        string coupon_user_id
        string user_id
        string benefit_method 
        string benefit_amount
        DateTime used_at
        DateTime created_at
        DateTime updated_at
    }
    T_COUPON_USER }o..|| T_USER: user_id 
```
* benefit_method는 아래 값을만을 가짐 
    * DISCOUNT_FIXED_AMOUNT
    * DISCOUNT_PERCENTAGE
* used_at은 NULLABLE 
    * 쿠폰이 사용된 경우에만 사용 시점을 저장함 

* benefit_amount는 benefit_method에 따른 할인 양을 저장함
    * e.g.) <br>
    method = "DISCOUNT_FIXED_AMOUNT", amount = "100" 일 경우<br>
    100포인트 할인

#### 설계 이유
* **쿠폰 복사 방식**: 쿠폰 발급 시 쿠폰 이벤트의 정보를 복사하여 저장합니다. 이는 쿠폰 이벤트가 변경되더라도 이미 발급된 쿠폰의 혜택은 변경되지 않도록 하기 위함입니다.
* **유연한 할인 방식**: benefit_method와 benefit_amount를 분리하여 다양한 할인 정책(정액/정률)을 지원합니다.
* **사용 여부 추적**: used_at 필드를 통해 쿠폰 사용 여부를 명확히 관리합니다. NULL이면 미사용, 값이 있으면 사용된 쿠폰입니다.
* **문자열 타입 활용**: benefit_method를 enum 대신 string으로 저장하여 새로운 할인 방식이 추가되어도 스키마 변경 없이 대응 가능합니다.


### 쿠폰 이벤트 도메인
```mermaid
erDiagram
    T_COUPON_EVENT {
        string coupon_event_id
        string benefit_method 
        string benefit_amount
        number total_issue_amount
        number left_issue_amount
        DateTime created_at
        DateTime updated_at
    }
```

#### 설계 이유
* **쿠폰 발급 관리**: total_issue_amount와 left_issue_amount를 통해 쿠폰 발급 수량을 제한하고 관리할 수 있도록 설계했습니다.
* **쿠폰 템플릿 역할**: 이 테이블은 쿠폰의 템플릿 역할을 하며, 실제 사용자에게 발급될 때 T_COUPON_USER에 복사됩니다.
* **확장성 고려**: benefit_method와 benefit_amount를 통해 여러 유형의 할인 혜택을 설정할 수 있습니다.
* **동시성 제어**: left_issue_amount 필드를 통해 동시 요청 시에도 발급 수량을 초과하지 않도록 제어할 수 있습니다.

###  결제 도메인
```mermaid
erDiagram
    T_PAYMENT {
        string payment_id
        string user_id
        number total_payment_amount 
        DateTime created_at
        DateTime updated_at
    }

    T_PAYMENT }o..|| T_USER: user_id 
```

#### 설계 이유
* **주문과 분리**: 결제는 주문과 분리하여 관리합니다. 이를 통해 한 번의 결제로 여러 주문을 처리하거나, 주문 취소와 결제 환불을 독립적으로 관리할 수 있습니다.
* **최소 정보만 포함**: 결제 관련 민감 정보는 포함하지 않고, 필요한 경우 외부 결제 시스템과 연동할 수 있도록 최소한의 정보만 저장합니다.

### 주문 도메인   
```mermaid
erDiagram
    T_ORDER {
        string order_id
        string user_id
        string payment_id
        number total_amount
        number total_discount_amount
        number final_amount
        DateTime created_at
        DateTime updated_at
    }
    
    T_ORDER_ITEM {
        string order_item_id
        string order_id
        string product_id
        number amount 
        number unit_price
        number total_price
        DateTime created_at
        DateTime updated_at
    }

    T_ORDER_DISCOUNT {
        string order_discount_id
        string order_id
        string discount_type
        string discount_id
        number discount_amount
        DateTime created_at
        DateTime updated_at
    }

    T_ORDER }o..|| T_USER: user_id 
    T_ORDER ||..|| T_PAYMENT: payment_id 
    T_ORDER ||--|{ T_ORDER_ITEM: order_id
    T_ORDER_ITEM }o..|| T_PRODUCT: product_id 
    T_ORDER ||--|{ T_ORDER_DISCOUNT: order_id
```

* discount_type은 아래 값을만을 가짐
    * COUPON: 쿠폰 할인

* discount_id는 discount_type에 따라 다른 테이블의 ID를 참조
    * COUPON: T_COUPON_USER의 coupon_user_id

#### 설계 이유
* **주문 분리 구조**: 주문(T_ORDER), 주문 상품(T_ORDER_ITEM), 주문 할인(T_ORDER_DISCOUNT)을 별도 테이블로 분리하여 유연성과 확장성을 높였습니다.
* **금액 정보 중복 저장**: 
  * 주문 시점의 가격을 unit_price에 저장하여 나중에 상품 가격이 변경되어도 주문 내역은 유지됩니다.
  * total_amount, total_discount_amount, final_amount 등의 계산된 금액을 저장하여 조회 시 효율성을 높였습니다.
* **할인 타입 분리**: discount_type과 discount_id를 통해 다양한 할인 유형(쿠폰, 포인트 등)을 유연하게 지원할 수 있습니다.
* **주문-결제 관계**: payment_id를 통해 주문과 결제를 연결하면서도 독립적으로 관리할 수 있습니다.

