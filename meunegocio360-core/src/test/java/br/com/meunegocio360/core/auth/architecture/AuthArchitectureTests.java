package br.com.meunegocio360.core.auth.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "br.com.meunegocio360.core",
    importOptions = ImportOption.DoNotIncludeTests.class)
class AuthArchitectureTests {

  @ArchTest
  static final ArchRule domain_does_not_depend_on_outer_layers =
      noClasses()
          .that()
          .resideInAPackage("..auth.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..auth.api..",
              "..auth.application..",
              "..auth.infra..",
              "org.springframework..",
              "jakarta..");

  @ArchTest
  static final ArchRule application_does_not_depend_on_api_or_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("..auth.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..auth.api..", "..auth.infra..");

  @ArchTest
  static final ArchRule api_does_not_depend_on_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("..auth.api..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..auth.infra..");
}
