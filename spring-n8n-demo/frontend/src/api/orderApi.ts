const API_BASE_URL = "http://localhost:8081/api/orders";

export type OrderStatus = "NEW" | "PENDING_APPROVAL" | "APPROVED" | "REJECTED";

export interface Order {
  id: number;
  article: string;
  quantity: number;
  amount: number;
  customer: string;
  status: OrderStatus;
  createdAt: string;
}

export interface OrderRequest {
  article: string;
  quantity: number;
  amount: number;
  customer: string;
}

export async function fetchOrders(): Promise<Order[]> {
  const response = await fetch(API_BASE_URL);
  if (!response.ok) {
    throw new Error(`Bestellungen konnten nicht geladen werden: ${response.status}`);
  }
  return response.json();
}

export async function createOrder(order: OrderRequest): Promise<Order> {
  const response = await fetch(API_BASE_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  if (!response.ok) {
    throw new Error(`Bestellung konnte nicht angelegt werden: ${response.status}`);
  }
  return response.json();
}
