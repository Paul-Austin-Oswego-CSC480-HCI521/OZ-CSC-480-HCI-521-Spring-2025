import { IoMdClose } from "react-icons/io";

const NotificationItem = ({ notification, onRemove }) => (
    <div
      style={{
        position: "relative",
        padding: "12px 8px",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        borderBottom:
          notification.isLast ? "none" : "0.5px solid #ddd",
        margin: 0,
        lineHeight: "18px",
      }}
    >
      <span
        style={{
          fontSize: "13px",
          marginRight: "4px",
          flex: 1,
          overflow: "hidden",
          textOverflow: "ellipsis",
          padding: "0",
        }}
      >
        {notification.message}
      </span>
      <button
        onClick={onRemove}
        style={{
          background: "none",
          border: "none",
          cursor: "pointer",
          color: "#888",
          padding: 0,
          margin: 0,
        }}
        title="Remove notification"
      >
        <IoMdClose size={14} />
      </button>
    </div>
  );

  export default NotificationItem;