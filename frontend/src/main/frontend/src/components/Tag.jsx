export default function Tag({text}) {
  const tags = {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#F8FDF4",
    border: "2px solid #14AE5C",
    color: "black",
    borderRadius: "999px",
    padding: "0px 16px",
    fontSize: "14px",
    fontWeight: "500",
    fontFamily: "Inter",
  }

  return <div style={tags}>#{text}</div>
}