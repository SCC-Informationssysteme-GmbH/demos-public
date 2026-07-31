import { useCallback, useEffect, useState } from "react";
import { fetchOrders, type Order } from "../api/orderApi";
import { OrderList } from "../components/OrderList";

export function HistoryPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);

  const loadOrders = useCallback(async () => {
    try {
      setOrders(await fetchOrders());
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unbekannter Fehler");
    }
  }, []);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  return (
    <div className="page">
      <h1>Historie</h1>
      {error && <div className="banner error">{error}</div>}
      <OrderList orders={orders} onRefresh={loadOrders} />
    </div>
  );
}
