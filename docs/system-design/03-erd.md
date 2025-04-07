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
    }
```

###  결제 도메인
```mermaid
erDiagram
    T_PAYMENT {
        string payment_id
        string user_id
        number total_payment_amount 
    }
    
    T_PAYMENT_DETAIL {
        string payment_detail_id
        string payment_id
        string payment_method
        number payment_amount 
        string payment_detail
    }

    T_PAYMENT }o..|| T_USER: user_id 
    T_PAYMENT ||--|{T_PAYMENT_DETAIL: has
```

* payment_detail에는 payment_method별로 상세한 정보가 들어감
    * e.g.) <br>
    payment_method = "COUPON" <br>
    payment_detail = {"couponUserId": "0000-0000-0000-000"} 
    
    * e.g.) <br>
    payment_method = "USER_POINT" <br>
    payment_detail = {"userPointId": "0000-0000-0000-000"} 


### 주문 도메인   
```mermaid
erDiagram
    T_ORDER {
        string order_id
        string user_id
        string payment_id
    }
    
    T_ORDER_ITEM {
        string order_item_id
        string order_id
        string product_id
        number amount 
        number price
    }

    T_ORDER }o..|| T_USER: user_id 
    T_ORDER ||..|| T_PAYMENT: payment_id 
    T_ORDER ||--|{ T_ORDER_ITEM: order_id
    T_ORDER_ITEM }o..|| T_PRODUCT: product_id 
```

