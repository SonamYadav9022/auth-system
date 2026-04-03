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
  token: string;
  name: string;
  email: string;
  role: "USER" | "ADMIN";
}

//this is the change with source tree