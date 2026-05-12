import { useNavigate, useLocation } from "react-router-dom";

export default function ChatbotButton() {
  const navigate = useNavigate();
  const location = useLocation();

  if (location.pathname === "/chatbot") {
    return null;
  }

return (
  <div className="chatbot-wrapper">
    <div className="chatbot-label">HonestBot</div>
    <button
      className="chatbot-icon"
      onClick={() => navigate("/chatbot")}
      title="Let the robots decide your future!"
    >
      <img
        src="/images/HonestBot.png"
        className="chatbot-icon-img"
        alt="Chatbot Icon"
      />
    </button>
  </div>
);

}
