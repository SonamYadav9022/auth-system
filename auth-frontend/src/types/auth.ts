export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: "USER" | "ADMIN";
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  name: string;
  email: string;
  role: "USER" | "ADMIN";
}

