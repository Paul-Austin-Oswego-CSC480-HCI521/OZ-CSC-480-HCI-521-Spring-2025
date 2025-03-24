import Tag from "./Tag";

export default function QuoteViewModal({quote, close}) {

  const quoteStyle = {
    color: "#1E1E1E",
    fontFamily: "Inter",
    fontSize: "24px",
    fontStyle: "normal",
    fontWeight: "500",
    lineHeight: "normal"
  }

  return (
    <div className="modal show" style={{ display: "block" }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content" style={{ backgroundColor: "#F8FDF1", borderRadius: "10px" }}>
          <div className="modal-header">
            <div>
              {quote.tags.map((tag, i) => <Tag key={i} text={tag}/>)}
            </div>
            <button type="button" className="btn-close" onClick={close}></button>
          </div>
          <div className="modal-body">
            <p style={quoteStyle}>{'"' + quote.quote + '"'}</p>
            <p>{quote.author}</p>
          </div>
        </div>
      </div>
    </div>
  );
}