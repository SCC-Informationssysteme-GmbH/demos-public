import type { Order, OrderStatus } from "../api/orderApi";

interface Props {
  orders: Order[];
  onRefresh: () => void;
}

const STATUS_LABELS: Record<OrderStatus, string> = {
  NEW: "Neu",
  PENDING_APPROVAL: "Wartet auf Freigabe",
  APPROVED: "Genehmigt",
  REJECTED: "Abgelehnt",
};

const STATUS_CLASSES: Record<OrderStatus, string> = {
  NEW: "status-new",
  PENDING_APPROVAL: "status-pending",
  APPROVED: "status-approved",
  REJECTED: "status-rejected",
};

function formatAmount(amount: number) {
  return amount.toLocaleString("de-DE", { style: "currency", currency: "EUR" });
}

export function OrderList({ orders, onRefresh }: Props) {
  return (
    <section className="card">
      <div className="card-header">
        <button className="secondary-button" onClick={onRefresh}>
          Aktualisieren
        </button>
      </div>
      {orders.length === 0 ? (
        <p className="empty-state">Noch keine Bestellungen vorhanden.</p>
      ) : (
        <table className="order-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Artikel</th>
              <th>Menge</th>
              <th>Betrag</th>
              <th>Kunde</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>{order.id}</td>
                <td>{order.article}</td>
                <td>{order.quantity}</td>
                <td>{formatAmount(order.amount)}</td>
                <td>{order.customer}</td>
                <td>
                  <span className={`status-badge ${STATUS_CLASSES[order.status]}`}>
                    {STATUS_LABELS[order.status]}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
