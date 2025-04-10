# ERD
### 고려사항
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

