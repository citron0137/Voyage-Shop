#!/bin/bash

# 상품 순위 API 부하 테스트 실행 스크립트
# 사용법: ./run-loadtest.sh [명령어] [옵션]

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 스크립트 사용법 출력
show_usage() {
    echo -e "${BLUE}상품 순위 API 부하 테스트 실행 스크립트${NC}"
    echo -e "사용법: ./run-loadtest.sh [명령어] [옵션]"
    echo -e "또는:  ./run-loadtest.sh <k6스크립트 경로>"
    echo
    echo -e "명령어:"
    echo -e "  ${GREEN}start${NC}           테스트 환경 시작 (애플리케이션, DB, Redis, 모니터링 도구)"
    echo -e "  ${GREEN}stop${NC}            테스트 환경 중지 및 삭제"
    echo -e "  ${GREEN}data-gen${NC}        테스트 데이터 생성"
    echo -e "  ${GREEN}test${NC}            부하 테스트 실행"
    echo -e "  ${GREEN}help${NC}            이 도움말 표시"
    echo
    echo -e "데이터 생성 옵션(data-gen 명령어):"
    echo -e "  ${YELLOW}-js${NC}              JS 스크립트로 데이터 생성"
    echo -e "  ${YELLOW}-pg${NC}              PostgreSQL용 데이터 생성 (JS 스크립트 사용 시)"
    echo -e "  ${YELLOW}-sql${NC}             SQL 스크립트로 데이터 생성"
    echo -e "  ${YELLOW}-size=[숫자]${NC}      SQL 스크립트 사용 시 데이터 크기 (1000, 10000, 100000)"
    echo
    echo -e "테스트 옵션(test 명령어):"
    echo -e "  ${YELLOW}-basic${NC}           기본 성능 테스트 실행 (기본값)"
    echo -e "  ${YELLOW}-cache${NC}           캐시 효율성 테스트 실행"
    echo -e "  ${YELLOW}-spike${NC}           트래픽 스파이크 테스트 실행"
    echo
    echo -e "예시:"
    echo -e "  ./run-loadtest.sh start       # 테스트 환경 시작"
    echo -e "  ./run-loadtest.sh data-gen -sql -size=10000  # 10,000개 주문 SQL 데이터 생성"
    echo -e "  ./run-loadtest.sh test -cache # 캐시 효율성 테스트 실행"
    echo -e "  ./run-loadtest.sh stop        # 테스트 환경 중지 및 삭제"
}

# 테스트 환경 시작
start_environment() {
    echo -e "${BLUE}테스트 환경을 시작합니다...${NC}"
    docker-compose \
      -f docker-compose.yml \
      -f docker-compose.app.yml \
      -f docker-compose.monitoring.yml \
      up -d
    
    echo -e "${GREEN}테스트 환경이 시작되었습니다.${NC}"
    echo -e "Grafana 대시보드: ${YELLOW}http://localhost:3000${NC} (계정: admin / admin)"
}

# 테스트 환경 중지 및 삭제
stop_environment() {
    echo -e "${BLUE}테스트 환경을 중지하고 삭제합니다...${NC}"
    docker-compose \
      -f docker-compose.yml \
      -f docker-compose.app.yml \
      -f docker-compose.loadtest.yml \
      -f docker-compose.monitoring.yml \
      down
    
    echo -e "${GREEN}테스트 환경이 중지되고 삭제되었습니다.${NC}"
}

# JS 스크립트로 데이터 생성
generate_data_js() {
    local script="cache-test/order-data-generator.js"
    
    if [ "$1" == "-pg" ]; then
        script="cache-test/order-data-generator-postgres.js"
        echo -e "${BLUE}PostgreSQL용 데이터를 생성합니다...${NC}"
    else
        echo -e "${BLUE}MySQL용 데이터를 생성합니다...${NC}"
    fi
    
    K6_SCRIPT="$script" \
    docker-compose \
      -f docker-compose.yml \
      -f docker-compose.app.yml \
      -f docker-compose.monitoring.yml \
      -f docker-compose.loadtest.yml \
      up k6 --no-deps
    
    echo -e "${GREEN}데이터 생성이 완료되었습니다.${NC}"
}

# SQL 스크립트로 데이터 생성
generate_data_sql() {
    local size="10000"
    
    if [[ "$1" == -size=* ]]; then
        size="${1#*=}"
    fi
    
    local script="load-tests/cache-test/massive-order-generator-${size}.sql"
    echo -e "${BLUE}SQL 스크립트로 ${size}개 데이터를 생성합니다...${NC}"
    
    # 파일 존재 확인
    if [ ! -f "$script" ]; then
        echo -e "${RED}오류: $script 파일을 찾을 수 없습니다.${NC}"
        echo -e "${YELLOW}사용 가능한 크기: 1000, 10000, 100000${NC}"
        return 1
    fi
    
    docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus < "$script"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}데이터 생성이 완료되었습니다.${NC}"
    else
        echo -e "${RED}데이터 생성 중 오류가 발생했습니다.${NC}"
    fi
}

# 부하 테스트 실행
run_test() {
    local script="cache-test/order-item-rank-test.js"
    local test_type="기본 성능"
    
    if [ "$1" == "-cache" ]; then
        script="cache-test/order-item-rank-cache-test.js"
        test_type="캐시 효율성"
    elif [ "$1" == "-spike" ]; then
        script="cache-test/order-item-rank-spike-test.js"
        test_type="트래픽 스파이크"
    fi
    
    echo -e "${BLUE}${test_type} 테스트를 실행합니다...${NC}"
    
    # 미리 정의된 테스트의 경우 cache-test 디렉토리에 있는 것으로 가정
    echo -e "${YELLOW}k6 스크립트 경로: $script${NC}"
    
    K6_SCRIPT="$script" \
    docker-compose \
      -f docker-compose.yml \
      -f docker-compose.app.yml \
      -f docker-compose.loadtest.yml \
      -f docker-compose.monitoring.yml \
      up k6 --no-deps
    
    echo -e "${GREEN}${test_type} 테스트가 완료되었습니다.${NC}"
    echo -e "Grafana 대시보드에서 결과를 확인하세요: ${YELLOW}http://localhost:3000${NC}"
}

# K6 스크립트 직접 실행
run_k6_script() {
    local script_path="$1"
    
    echo -e "${YELLOW}스크립트 경로 확인: $script_path${NC}"
    
    # 상대 경로 처리 ('./' 시작하는 경로)
    if [[ "$script_path" == ./* ]]; then
        script_path="${script_path:2}"
    fi
    
    # 백슬래시를 슬래시로 변환 (윈도우 경로 호환성)
    script_path="${script_path//\\//}"
    
    echo -e "${YELLOW}처리된 스크립트 경로: $script_path${NC}"
    
    # 스크립트 파일 존재 확인
    if [ ! -f "$script_path" ]; then
        echo -e "${RED}오류: $script_path 파일을 찾을 수 없습니다.${NC}"
        
        # 가능한 경로 제안
        filename=$(basename "$script_path")
        possible_path="cache-test/$filename"
        if [ -f "$possible_path" ]; then
            echo -e "${YELLOW}다음 경로로 시도해 보세요: $possible_path${NC}"
        fi
        
        return 1
    fi
    
    echo -e "${BLUE}k6 스크립트를 직접 실행합니다: $script_path${NC}"
    
    # 파일 이름 추출 및 Docker 볼륨 매핑에 따른 경로 수정
    filename=$(basename "$script_path")
    
    # Docker-Compose는 ./load-tests를 컨테이너 내부의 /tests로 매핑
    # 스크립트가 load-tests 디렉토리에 있으면 경로 조정
    if [[ "$script_path" == load-tests/* ]]; then
        # load-tests 디렉토리 내부의 스크립트, /tests에 상대적인 경로 사용
        relative_path="${script_path#load-tests/}"
        echo -e "${YELLOW}load-tests 디렉토리 내 스크립트 감지${NC}"
        echo -e "${YELLOW}컨테이너 내부 경로: $relative_path${NC}"
        K6_SCRIPT="$relative_path" \
        docker-compose \
          -f docker-compose.yml \
          -f docker-compose.app.yml \
          -f docker-compose.loadtest.yml \
          -f docker-compose.monitoring.yml \
          up k6 --no-deps
    elif [[ "$script_path" =~ cache-test/.*\.js$ ]]; then
        # cache-test 디렉토리 내부의 스크립트
        echo -e "${YELLOW}cache-test 디렉토리 내 스크립트 감지${NC}"
        K6_SCRIPT="$script_path" \
        docker-compose \
          -f docker-compose.yml \
          -f docker-compose.app.yml \
          -f docker-compose.loadtest.yml \
          -f docker-compose.monitoring.yml \
          up k6 --no-deps
    else
        # 기본값: 파일 이름만 사용
        echo -e "${YELLOW}k6 실행에 사용할 스크립트 이름: $filename${NC}"
        K6_SCRIPT="$filename" \
        docker-compose \
          -f docker-compose.yml \
          -f docker-compose.app.yml \
          -f docker-compose.loadtest.yml \
          -f docker-compose.monitoring.yml \
          up k6 --no-deps
    fi
    
    echo -e "${GREEN}테스트가 완료되었습니다.${NC}"
    echo -e "Grafana 대시보드에서 결과를 확인하세요: ${YELLOW}http://localhost:3000${NC}"
}

# 메인 로직
main() {
    if [ $# -eq 0 ]; then
        show_usage
        exit 0
    fi
    
    # 첫 번째 인자가 파일 경로인지 확인
    if [[ "$1" == *.js ]]; then
        # 상대 경로 처리
        script_path="$1"
        if [[ "$script_path" == ./* ]]; then
            script_path="${script_path:2}"
        fi
        
        # 백슬래시를 슬래시로 변환
        script_path="${script_path//\\//}"
        
        echo -e "${YELLOW}스크립트 실행 시도: $script_path${NC}"
        
        # 파일 존재 확인
        if [ -f "$script_path" ]; then
            run_k6_script "$script_path"
            exit 0
        else
            echo -e "${RED}파일을 찾을 수 없습니다: $script_path${NC}"
            # 가능한 경로 제안
            filename=$(basename "$script_path")
            possible_path="cache-test/$filename"
            if [ -f "$possible_path" ]; then
                echo -e "${YELLOW}다음 경로로 시도해 보세요: $possible_path${NC}"
            fi
        fi
    fi
    
    case "$1" in
        "start")
            start_environment
            ;;
        "stop")
            stop_environment
            ;;
        "data-gen")
            if [ "$2" == "-js" ]; then
                generate_data_js "$3"
            elif [ "$2" == "-sql" ]; then
                generate_data_sql "$3"
            else
                echo -e "${RED}오류: 데이터 생성 옵션을 지정해주세요.${NC}"
                echo -e "사용 가능한 옵션: -js, -sql"
                exit 1
            fi
            ;;
        "test")
            run_test "$2"
            ;;
        "help")
            show_usage
            ;;
        *)
            echo -e "${RED}오류: 알 수 없는 명령어입니다.${NC}"
            show_usage
            exit 1
            ;;
    esac
}

# 스크립트 실행
main "$@" 