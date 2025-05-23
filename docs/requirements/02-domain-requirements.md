# 🏗️ 도메인 요구사항

## 💰 사용자 포인트 도메인

| 항목 | 설명 |
|------|------|
| **개요** | 사용자별로 포인트 잔액을 저장 |
| **기능** | • 사용자가 생성될 때 사용자 포인트도 생성<br>• 포인트 양을 증가하거나 차감 가능<br>• 사용자 삭제(탈퇴) 시에도 데이터 유지 |
| **정책** | • 포인트 잔액은 상한선 존재<br>• 포인트 잔액은 0원 미만이 될 수 없음<br>• 유저별로 유저 포인트는 하나만 생성 가능 |

## 🛒 상품 도메인

| 항목 | 설명 |
|------|------|
| **개요** | 상품은 ID, 가격, 재고 수량 속성 보유 |
| **기능** | • 재고 수량은 주문에 따라 감소 가능<br>• 재고 수량은 증가 가능<br>• 주문 내역 보존을 위해 상품 삭제 없음 (재고를 0으로 처리) |
| **정책** | • 재고 수량은 0 미만이 될 수 없음<br>• 재고 수량은 상한선 존재<br>• 가격은 0 이하가 될 수 없음 |

## 🎫 쿠폰 사용자 도메인

| 항목 | 설명 |
|------|------|
| **개요** | 쿠폰 사용자는 사용자 ID, 할인방법, 할인정도, 사용 여부 정보 보유 |
| **기능** | • 쿠폰 생성 가능 (기본 상태: 사용 이전)<br>• 쿠폰 사용 처리 가능<br>• 쿠폰 사용 취소 가능<br>• 할인 방법과 정도에 따른 할인 금액 계산 |
| **정책** | • 사용된 쿠폰은 재사용 불가 |

## 🎟️ 쿠폰 이벤트 도메인

| 항목 | 설명 |
|------|------|
| **개요** | 쿠폰 이벤트는 할인 방법, 할인 정도, 총 발급 수량, 잔여 발급 수량 정보 보유 |
| **기능** | • 쿠폰 이벤트 생성 가능<br>• 잔여 발급 수량 감소 가능 |
| **정책** | • 잔여 발급 수량은 0 미만이 될 수 없음 |

## 💳 결제 도메인

| 항목 | 설명 |
|------|------|
| **개요** | 결제 금액 정보 보유 |
| **기능** | • 결제 완료 시점에 생성 가능<br>• 수정이나 삭제 불가 |
| **정책** | • 결제 금액은 0 이하가 될 수 없음 |

## 📦 주문 도메인

| 항목 | 설명 |
|------|------|
| **개요** | 주문 유저 ID, 주문 상품 ID별 주문 개수, 쿠폰 사용 정보, 금액 정보 보유 |
| **기능** | • 주문 완료 시점에 생성 가능<br>• 수정이나 삭제 불가<br>• 상품 ID별 주문 개수 집계 및 조회 기능 |
| **정책** | • 주문 상품 ID별 주문 개수는 0 이하가 될 수 없음 | 