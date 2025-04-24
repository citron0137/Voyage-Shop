# 📚 Voyage Shop 문서

이 디렉토리는 Voyage Shop 프로젝트의 모든 문서를 포함하고 있습니다.

## 폴더 구조

- **[requirements/](./requirements)**: 프로젝트 요구사항 문서
  - [기능적 요구사항](./requirements/01-functional-requirements.md)
  - [도메인 요구사항](./requirements/02-domain-requirements.md)
  - [비기능적 요구사항](./requirements/03-non-functional-requirements.md)

- **[design/](./design)**: 시스템 설계 문서
  - [데이터베이스 설계](./design/database-design.md)
  - [ERD 다이어그램](./design/erd.md)
  - [시퀀스 다이어그램](./design/sequence-diagram.md)

- **[conventions/](./conventions)**: 프로젝트 코드 및 개발 컨벤션
  - [공통 컨벤션](./conventions/common-conventions.md)
  - [레이어드 아키텍처](./conventions/layered-architecture.md)
  - [패키지 구조](./conventions/package-structure.md)
  - [컨트롤러 레이어](./conventions/controller-layer.md)
  - [애플리케이션 레이어](./conventions/application-layer.md)
  - [도메인 레이어](./conventions/domain-layer.md)
  - [테스트 컨벤션](./conventions/test-conventions.md)

- **[implementation-steps/](./implementation-steps)**: 구현 단계별 문서
  - [7단계: JPA 리포지토리 구현 및 통합 테스트](./implementation-steps/STEP-07.md)

- **[implementation-guide/](./implementation-guide)**: 구현 가이드 및 예제

- **[archive/](./archive)**: 이전 문서 아카이브 (참조용)

## 문서 작성 가이드라인

1. **마크다운 형식**: 모든 문서는 마크다운(.md) 형식으로 작성합니다.
2. **문서 제목**: 문서 시작 시 적절한 제목(h1)을 포함합니다.
3. **이모지 활용**: 주요 섹션에는 관련 이모지를 사용하여 가독성을 높입니다.
4. **참조 링크**: 다른 문서를 참조할 때는 상대 경로 링크를 사용합니다.
5. **코드 예제**: 코드 예제는 적절한 언어 표시와 함께 코드 블록으로 작성합니다.
6. **다이어그램**: 다이어그램은 가능한 Mermaid 형식으로 작성하여 버전 관리가 용이하도록 합니다.

## 문서 업데이트 정책

- 모든 주요 기능 개발 전에 관련 문서를 먼저 업데이트합니다.
- 설계 변경 시 관련 문서를 즉시 업데이트합니다.
- PR에 문서 변경 사항이 포함된 경우, PR 설명에 이를 명시합니다. 