import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { OrderForm } from "../components/OrderForm";

export function NewOrderPage() {
  const [justCreated, setJustCreated] = useState(false);
  const navigate = useNavigate();

  function handleOrderCreated() {
    setJustCreated(true);
  }

  return (
    <div className="page">
      <h1>Neue Bestellung</h1>
      {justCreated && (
        <div className="banner success">
          Bestellung wurde angelegt.{" "}
          <button className="link-button" onClick={() => navigate("/historie")}>
            Zur Historie
          </button>
        </div>
      )}
      <OrderForm onOrderCreated={handleOrderCreated} />
    </div>
  );
}
