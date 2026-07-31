import { useState, type FormEvent } from "react";
import { createOrder, type OrderRequest } from "../api/orderApi";

interface Props {
  onOrderCreated: () => void;
}

export function OrderForm({ onOrderCreated }: Props) {
  const [article, setArticle] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [amount, setAmount] = useState(0);
  const [customer, setCustomer] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    const order: OrderRequest = { article, quantity, amount, customer };

    try {
      await createOrder(order);
      setArticle("");
      setQuantity(1);
      setAmount(0);
      setCustomer("");
      onOrderCreated();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unbekannter Fehler");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="card order-form" onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="article">Artikel</label>
        <input id="article" value={article} onChange={(e) => setArticle(e.target.value)} required />
      </div>
      <div className="field">
        <label htmlFor="quantity">Menge</label>
        <input
          id="quantity"
          type="number"
          min={1}
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
          required
        />
      </div>
      <div className="field">
        <label htmlFor="amount">Betrag (EUR)</label>
        <input
          id="amount"
          type="number"
          min={0}
          step="0.01"
          value={amount}
          onChange={(e) => setAmount(Number(e.target.value))}
          required
        />
      </div>
      <div className="field">
        <label htmlFor="customer">Kunde</label>
        <input id="customer" value={customer} onChange={(e) => setCustomer(e.target.value)} required />
      </div>
      <button type="submit" className="primary-button" disabled={submitting}>
        {submitting ? "Wird angelegt..." : "Bestellung anlegen"}
      </button>
      {error && (
        <div className="banner error" role="alert">
          {error}
        </div>
      )}
    </form>
  );
}
