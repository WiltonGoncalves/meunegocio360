package br.com.meunegocio360.core.shared.error;

public class ErroDeNegocio extends RuntimeException {

  private final int status;
  private final String code;
  private final String title;

  public ErroDeNegocio(int status, String code, String title, String detail) {
    super(detail);
    this.status = status;
    this.code = code;
    this.title = title;
  }

  public int getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getTitle() {
    return title;
  }
}
