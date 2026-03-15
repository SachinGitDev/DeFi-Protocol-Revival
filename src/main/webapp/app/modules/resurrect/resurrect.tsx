import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Spinner, Badge, Card, CardBody } from 'reactstrap';
import { useLocation } from 'react-router-dom';
import axios from 'axios';

interface MisinformationDetail {
  claim: string;
  reality: string;
  severity: string;
}

interface MisinformationReport {
  accuracyScore: number;
  accuracyVerdict: string;
  misinformationSummary: string;
  misinformationDetails: MisinformationDetail[];
  overallRiskScore: number;
  overallRiskVerdict: string;
}

export const Resurrect = () => {
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const contractId = queryParams.get('contractId') || queryParams.get('id') || 1001;
  const repoUrl = queryParams.get('repo') || '';

  const [contractData, setContractData] = useState<any>(null);
  const [misinfo, setMisinfo] = useState<MisinformationReport | null>(null);
  const [misinfoLoading, setMisinfoLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [analysis, setAnalysis] = useState<{ oldScore: number; newScore: number; overallScore: number; rationale: string } | null>(null);
  const [analysisLoading, setAnalysisLoading] = useState(false);

  const getSeverityColor = (severity: string) => {
    const s = severity?.toUpperCase();
    if (s === 'CRITICAL' || s === 'HIGH') return '#dc3545';
    if (s === 'MEDIUM') return '#fd7e14';
    return '#6c757d';
  };

  const getSeverityBadge = (severity: string) => {
    const s = severity?.toUpperCase();
    if (s === 'CRITICAL') return 'danger';
    if (s === 'HIGH') return 'warning';
    if (s === 'MEDIUM') return 'info';
    return 'secondary';
  };

  const getVerdictColor = (verdict: string) => {
    const v = verdict?.toUpperCase();
    if (v === 'FRAUDULENT' || v === 'CRITICAL') return '#dc3545';
    if (v === 'MISLEADING' || v === 'HIGH RISK') return '#fd7e14';
    if (v === 'LOW RISK' || v === 'MEDIUM RISK') return '#ffc107';
    return '#28a745';
  };

  useEffect(() => {
    const animationTimer = new Promise(resolve => setTimeout(resolve, 3000));

    const token = localStorage.getItem('jhi-authenticationToken') || sessionStorage.getItem('jhi-authenticationToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    const fetchContract = axios.get(`/api/smart-contracts/${contractId}`, { headers }).then(res => res.data);

    Promise.all([animationTimer, fetchContract])
      .then(([_animationComplete, contractRes]) => {
        if (contractRes) {
          contractRes.originalCode = contractRes.originalCode
            ?.replace(/__/g, '\n\n')
            .replace(/_(?=[^_])/g, '\n')
            .replace(/_$/g, '\n');
          contractRes.resurrectedCode = contractRes.resurrectedCode
            ?.replace(/__/g, '\n\n')
            .replace(/_(?=[^_])/g, '\n')
            .replace(/_$/g, '\n');
        }
        setContractData(contractRes);
        if (contractRes?.originalCode && contractRes?.resurrectedCode) {
          setAnalysisLoading(true);
          axios
            .post(
              '/api/analysis',
              {
                oldCode: contractRes.originalCode,
                newCode: contractRes.resurrectedCode,
                misinformationScore: 0, // will be updated once misinfo loads
              },
              { headers },
            )
            .then(res => setAnalysis(res.data))
            .catch(() => setAnalysis(null))
            .finally(() => setAnalysisLoading(false));
        }
        setLoading(false);

        // Fetch misinformation analysis after contract loads
        if (repoUrl) {
          setMisinfoLoading(true);
          axios
            .get(`/api/analyse?repoUrl=${encodeURIComponent(repoUrl)}`)
            .then(res => {
              let data = res.data;
              if (typeof data === 'string') {
                // Strip code fences then replace _ newline delimiters
                let cleaned = data.replace(/```json\n?|```/g, '').trim();
                // The backend uses _ as newline delimiter - replace them
                cleaned = cleaned.replace(/_/g, '\n');
                try {
                  data = JSON.parse(cleaned);
                } catch {
                  // If that fails, try without replacement
                  const raw = data.replace(/```json\n?|```/g, '').trim();
                  data = JSON.parse(raw);
                }
              }
              setMisinfo(data);
            })
            .catch(() => setMisinfo(null))
            .finally(() => setMisinfoLoading(false));
        }
      })
      .catch(error => {
        console.error('Error fetching data:', error);
        setFetchError(`Failed to load contract: ${error.message || error}`);
        setLoading(false);
      });
  }, [contractId]);

  if (loading) {
    return (
      <Container className="text-center mt-5 pt-5">
        <div style={{ height: '300px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
          <Spinner color="success" style={{ width: '8rem', height: '8rem', borderWidth: '0.5rem' }} className="mb-4" />
          <h1 className="text-success mt-4">Resurrecting Protocol...</h1>
          <p className="text-muted lead">Re-writing vulnerable logic and clearing misinformation.</p>
        </div>
      </Container>
    );
  }

  if (fetchError) {
    return (
      <Container className="text-center mt-5 pt-5">
        <div style={{ color: '#dc3545', fontSize: '1.2rem' }}>
          <h3>Error Loading Contract</h3>
          <p>{fetchError}</p>
          <p className="text-muted">Contract ID attempted: {contractId}</p>
        </div>
      </Container>
    );
  }

  return (
    <Container fluid className="mt-4 px-5">
      <div className="text-center mb-5">
        <h1 className="display-4 font-weight-bold">{contractData?.name || 'Contract'} Resurrected</h1>
        <p className="lead text-muted">Vulnerabilities patched. Ready for deployment.</p>
      </div>
      {/* OVERALL SCORE SECTION */}
      <Row className="justify-content-center mt-5 mb-4">
        <Col md="10">
          <Card className="shadow-sm border-0" style={{ borderRadius: '15px' }}>
            <CardBody className="p-4">
              <h4 className="mb-4 border-bottom pb-2">Code Security Analysis</h4>

              {analysisLoading && (
                <div className="text-center py-4">
                  <Spinner color="info" />
                  <p className="text-muted mt-2">Analysing code security...</p>
                </div>
              )}

              {!analysisLoading && !analysis && <p className="text-muted">No code analysis available.</p>}

              {!analysisLoading && analysis && (
                <>
                  <div className="d-flex gap-4 mb-4 flex-wrap justify-content-center">
                    {/* Old Score */}
                    <div className="p-3 rounded text-white text-center" style={{ backgroundColor: '#dc3545', minWidth: '160px' }}>
                      <div style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>{analysis.oldScore}</div>
                      <div style={{ fontSize: '13px' }}>Original Code Score</div>
                    </div>

                    {/* Arrow */}
                    <div className="d-flex align-items-center" style={{ fontSize: '2rem', color: '#6c757d' }}>
                      →
                    </div>

                    {/* New Score */}
                    <div className="p-3 rounded text-white text-center" style={{ backgroundColor: '#28a745', minWidth: '160px' }}>
                      <div style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>{analysis.newScore}</div>
                      <div style={{ fontSize: '13px' }}>Revived Code Score</div>
                    </div>

                    {/* Overall Score */}
                    <div className="p-3 rounded text-white text-center" style={{ backgroundColor: '#007bff', minWidth: '160px' }}>
                      <div style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>{analysis.overallScore}</div>
                      <div style={{ fontSize: '13px' }}>Overall Score</div>
                    </div>
                  </div>

                  {/* Rationale */}
                  {analysis.rationale && (
                    <div className="p-3 rounded" style={{ backgroundColor: '#f8f9fa' }}>
                      <strong style={{ fontSize: '13px', color: '#495057' }}>AI Rationale:</strong>
                      <p className="mb-0 mt-1" style={{ fontSize: '13px', color: '#6c757d', lineHeight: '1.6' }}>
                        {analysis.rationale}
                      </p>
                    </div>
                  )}
                </>
              )}
            </CardBody>
          </Card>
        </Col>
      </Row>

      {/* MISINFORMATION SECTION */}
      <Row className="justify-content-center mb-5">
        <Col md="10">
          <Card className="shadow-sm border-0" style={{ borderRadius: '15px' }}>
            <CardBody className="p-4">
              <h4 className="mb-3 border-bottom pb-2">README Misinformation Analysis</h4>

              {misinfoLoading && (
                <div className="text-center py-4">
                  <Spinner color="warning" />
                  <p className="text-muted mt-2">Analysing README for misinformation...</p>
                </div>
              )}

              {!misinfoLoading && !misinfo && repoUrl && <p className="text-muted">No misinformation analysis available.</p>}

              {!misinfoLoading && !repoUrl && <p className="text-muted">No repository URL provided for analysis.</p>}

              {!misinfoLoading && misinfo && (
                <>
                  {/* Score Row */}
                  <div className="d-flex gap-4 mb-4 flex-wrap">
                    <div
                      className="p-3 rounded text-white text-center"
                      style={{ backgroundColor: getVerdictColor(misinfo.accuracyVerdict), minWidth: '160px' }}
                    >
                      <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>{misinfo.accuracyScore}/100</div>
                      <div style={{ fontSize: '13px' }}>README Accuracy</div>
                      <div style={{ fontSize: '12px', fontWeight: 'bold' }}>{misinfo.accuracyVerdict}</div>
                    </div>
                    <div
                      className="p-3 rounded text-white text-center"
                      style={{ backgroundColor: getVerdictColor(misinfo.overallRiskVerdict), minWidth: '160px' }}
                    >
                      <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>{misinfo.overallRiskScore}/100</div>
                      <div style={{ fontSize: '13px' }}>Overall Risk</div>
                      <div style={{ fontSize: '12px', fontWeight: 'bold' }}>{misinfo.overallRiskVerdict}</div>
                    </div>
                    <div className="p-3 rounded flex-grow-1" style={{ backgroundColor: '#f8f9fa', minWidth: '200px' }}>
                      <div style={{ fontSize: '13px', fontWeight: 'bold', color: '#495057', marginBottom: '4px' }}>Summary</div>
                      <div style={{ fontSize: '13px', color: '#6c757d', lineHeight: '1.5' }}>{misinfo.misinformationSummary}</div>
                    </div>
                  </div>

                  {/* Misinformation Details */}
                  {misinfo.misinformationDetails?.length > 0 && (
                    <>
                      <h5 className="mb-3" style={{ color: '#495057' }}>
                        Identified Misinformation ({misinfo.misinformationDetails.length})
                      </h5>
                      {misinfo.misinformationDetails.map((item, idx) => (
                        <div
                          key={idx}
                          className="mb-3 p-3 border rounded"
                          style={{ borderLeft: `4px solid ${getSeverityColor(item.severity)} !important`, backgroundColor: '#fff' }}
                        >
                          <div className="d-flex justify-content-between align-items-start mb-2">
                            <span style={{ fontWeight: 'bold', fontSize: '13px', color: '#495057' }}>#{idx + 1} Claim</span>
                            <Badge color={getSeverityBadge(item.severity)} style={{ fontSize: '11px', padding: '4px 8px' }}>
                              {item.severity}
                            </Badge>
                          </div>
                          <div className="mb-2 p-2 rounded" style={{ backgroundColor: '#fff3cd', fontSize: '13px' }}>
                            <strong>README Claims:</strong> {item.claim}
                          </div>
                          <div className="p-2 rounded" style={{ backgroundColor: '#f8d7da', fontSize: '13px' }}>
                            <strong>Reality:</strong> {item.reality}
                          </div>
                        </div>
                      ))}
                    </>
                  )}
                </>
              )}
            </CardBody>
          </Card>
        </Col>
      </Row>

      {/* CODE SPLIT SCREEN */}
      <Row>
        {/* OLD CODE BOX */}
        <Col md="6">
          <div className="shadow-lg h-100" style={{ borderRadius: '15px', overflow: 'hidden' }}>
            <div className="bg-danger text-white p-3 font-weight-bold">ORIGINAL VULNERABLE CONTRACT</div>
            <div className="bg-dark text-white p-4 h-100" style={{ minHeight: '600px', overflowY: 'auto' }}>
              <pre style={{ color: '#ffb3b3', fontFamily: '"Courier New", Courier, monospace', fontSize: '14px', whiteSpace: 'pre-wrap' }}>
                {contractData?.originalCode || 'No original code found.'}
              </pre>
            </div>
          </div>
        </Col>

        {/* NEW CODE BOX */}
        <Col md="6">
          <div className="shadow-lg h-100" style={{ borderRadius: '15px', overflow: 'hidden' }}>
            <div className="bg-success text-white p-3 font-weight-bold">REVIVED SECURE CONTRACT</div>
            <div className="bg-dark text-white p-4 h-100" style={{ minHeight: '600px', overflowY: 'auto' }}>
              <pre style={{ color: '#b3ffb3', fontFamily: '"Courier New", Courier, monospace', fontSize: '14px', whiteSpace: 'pre-wrap' }}>
                {contractData?.resurrectedCode || 'No resurrected code found.'}
              </pre>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
};

export default Resurrect;
