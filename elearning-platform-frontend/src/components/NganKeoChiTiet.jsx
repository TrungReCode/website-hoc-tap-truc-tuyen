export default function NganKeoChiTiet({
  mo,
  onDong,
  tieuDe,
  children,
  rongPx = 480,
  zIndex = 1200,
}) {
  if (!mo) return null;

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex,
        display: 'flex',
        justifyContent: 'flex-end',
      }}
    >
      {/* Lớp phủ mờ — bấm ra ngoài để đóng */}
      <div
        onClick={onDong}
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(0,0,0,.35)',
          animation: 'fadeIn .15s ease-out',
        }}
      />

      {/* Panel trượt vào từ bên phải */}
      <div
        style={{
          position: 'relative',
          width: rongPx,
          maxWidth: '92vw',
          height: '100%',
          background: 'var(--bg, #fff)',
          boxShadow: '-8px 0 24px rgba(0,0,0,.12)',
          display: 'flex',
          flexDirection: 'column',
          animation: 'truotVaoTuPhai .2s ease-out',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            padding: '16px 20px',
            borderBottom: '1px solid var(--border)',
            flexShrink: 0,
          }}
        >
          <div
            style={{
              fontWeight: 600,
              fontSize: 15,
              flex: 1,
            }}
          >
            {tieuDe}
          </div>

          <button
            className="btn btn-secondary btn-sm"
            onClick={onDong}
          >
            <i className="ti ti-x" aria-hidden="true"></i>
          </button>
        </div>

        <div
          style={{
            flex: 1,
            overflowY: 'auto',
            padding: 20,
          }}
        >
          {children}
        </div>
      </div>

      <style>{`
        @keyframes truotVaoTuPhai {
          from {
            transform: translateX(100%);
          }
          to {
            transform: translateX(0);
          }
        }

        @keyframes fadeIn {
          from {
            opacity: 0;
          }
          to {
            opacity: 1;
          }
        }
      `}</style>
    </div>
  );
}